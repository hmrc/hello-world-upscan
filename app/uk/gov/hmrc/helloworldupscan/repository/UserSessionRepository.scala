/*
 * Copyright 2021 HM Revenue & Customs
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


import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Updates.set
import play.api.libs.json._
import uk.gov.hmrc.helloworldupscan.connectors.Reference
import uk.gov.hmrc.helloworldupscan.model._
import uk.gov.hmrc.helloworldupscan.repository.UploadDetails._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class UploadDetails(uploadId: UploadId, reference: Reference, status: UploadStatus)

object UploadDetails {
  val status = "status"

  val uploadedSuccessfullyFormat: OFormat[UploadedSuccessfully] = Json.format[UploadedSuccessfully]

  val read: Reads[UploadStatus] = new Reads[UploadStatus] {
    override def reads(json: JsValue): JsResult[UploadStatus] = {
      val jsObject = json.asInstanceOf[JsObject]
      jsObject.value.get("_type") match {
        case Some(JsString("InProgress")) => JsSuccess(InProgress)
        case Some(JsString("Failed")) => JsSuccess(Failed)
        case Some(JsString("UploadedSuccessfully")) => Json.fromJson[UploadedSuccessfully](jsObject)(uploadedSuccessfullyFormat)
        case Some(value) => JsError(s"Unexpected value of _type: $value")
        case None => JsError("Missing _type field")
      }
    }
  }

  val write: Writes[UploadStatus] = new Writes[UploadStatus] {
    override def writes(p: UploadStatus): JsValue = {
      p match {
        case InProgress => JsObject(Map("_type" -> JsString("InProgress")))
        case Failed => JsObject(Map("_type" -> JsString("Failed")))
        case s: UploadedSuccessfully => Json.toJson(s)(uploadedSuccessfullyFormat).as[JsObject] + ("_type" -> JsString("UploadedSuccessfully"))
      }
    }
  }

  implicit val uploadStatusFormat: Format[UploadStatus] = Format(read, write)

  implicit val idFormat: OFormat[UploadId] = Json.format[UploadId]

  implicit val referenceFormat: OFormat[Reference] = Json.format[Reference]

  val format: Format[UploadDetails] = Json.format[UploadDetails]
}

class UserSessionRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[UploadDetails](
    collectionName = "simpleTestRepository",
    mongoComponent = mongoComponent,
    domainFormat = UploadDetails.format,
    indexes = Seq()
  ) {

  def insert(details: UploadDetails) = collection.insertOne(details).toFuture()

  def findByUploadId(uploadId: UploadId): Future[Option[UploadDetails]] =
    collection.find(Filters.eq("_uploadId", uploadId.value)).headOption

  def updateStatus(reference: Reference, newStatus: UploadStatus): Future[UploadStatus] = {
    for (
      result <- collection.findOneAndUpdate(
        Filters.equal("reference", Json.toJson(reference)),
        set("status", Json.toJson(newStatus))
      ).toFutureOption()
    ) yield {
      result.map(_.status).getOrElse(throw new Exception("Update failed, no document modified"))
    }
  }


}
