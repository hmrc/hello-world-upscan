/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helloworldupscan.repository


import org.bson.types.ObjectId
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions, Indexes}
import org.mongodb.scala.SingleObservableFuture
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.helloworldupscan.connectors.Reference
import uk.gov.hmrc.helloworldupscan.model.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoFormats
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.net.{URI, URL}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object UserSessionRepository:
  val status = "status"

  private given Format[UploadStatus] =
    given Format[URL] = summon[Format[String]].inmap(new URI(_).toURL, _.toString)
    given Format[UploadStatus.UploadedSuccessfully] =
      Json.format[UploadStatus.UploadedSuccessfully]
    val read: Reads[UploadStatus] =
      (json: JsValue) =>
        val jsObject = json.asInstanceOf[JsObject]
        jsObject.value.get("_type") match
          case Some(JsString("InProgress"))           => JsSuccess(UploadStatus.InProgress)
          case Some(JsString("Failed"))               => JsSuccess(UploadStatus.Failed)
          case Some(JsString("UploadedSuccessfully")) => Json.fromJson[UploadStatus.UploadedSuccessfully](jsObject)
          case Some(value)                            => JsError(s"Unexpected value of _type: $value")
          case None                                   => JsError("Missing _type field")

    val write: Writes[UploadStatus] =
      case UploadStatus.InProgress              => JsObject(Map("_type" -> JsString("InProgress")))
      case UploadStatus.Failed                  => JsObject(Map("_type" -> JsString("Failed")))
      case s: UploadStatus.UploadedSuccessfully => Json.toJson(s).as[JsObject]
                                                   + ("_type" -> JsString("UploadedSuccessfully"))

    Format(read, write)

  private given Format[UploadId] =
    Format.at[String](__ \ "value")
      .inmap[UploadId](UploadId.apply, _.value)

  private given Format[Reference] =
    Format.at[String](__ \ "value")
      .inmap[Reference](Reference.apply, _.value)

  private[repository] val mongoFormat: Format[UploadDetails] =
    given Format[ObjectId] = MongoFormats.objectIdFormat
    ( (__ \ "_id"      ).format[ObjectId]
    ~ (__ \ "uploadId" ).format[UploadId]
    ~ (__ \ "reference").format[Reference]
    ~ (__ \ "status"   ).format[UploadStatus]
    )(UploadDetails.apply, Tuple.fromProductTyped _)

@Singleton
class UserSessionRepository @Inject()(
  mongoComponent: MongoComponent
)(using
  ExecutionContext
) extends PlayMongoRepository[UploadDetails](
  collectionName = "simpleTestRepository",
  mongoComponent = mongoComponent,
  domainFormat   = UserSessionRepository.mongoFormat,
  indexes        = Seq(
                     IndexModel(Indexes.ascending("uploadId"), IndexOptions().unique(true)),
                     IndexModel(Indexes.ascending("reference"), IndexOptions().unique(true))
                   ),
  replaceIndexes = true
):
  import UserSessionRepository.given

  override lazy val requiresTtlIndex: Boolean = false // example repo, never deployed to prod

  def insert(details: UploadDetails): Future[Unit] =
    collection.insertOne(details)
      .toFuture()
      .map(_ => ())

  def findByUploadId(uploadId: UploadId): Future[Option[UploadDetails]] =
    collection.find(equal("uploadId", Codecs.toBson(uploadId))).headOption()

  def updateStatus(reference: Reference, newStatus: UploadStatus): Future[UploadStatus] =
    collection
      .findOneAndUpdate(
        filter  = equal("reference", Codecs.toBson(reference)),
        update  = set("status", Codecs.toBson(newStatus)),
        options = FindOneAndUpdateOptions().upsert(true)
      )
      .toFuture()
      .map(_.status)
