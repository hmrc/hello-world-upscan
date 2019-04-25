/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.upscan.services

import java.util.UUID

import akka.actor.ActorSystem
import javax.inject.Inject
import uk.gov.hmrc.helloworldupscan.services.UpscanCallbackDispatcher

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class UpscanFileReference(reference : String)

case class UpscanInitiateResponse(fileReference: UpscanFileReference, postTarget : String, formFields : Map[String, String])

case class UploadCallback(fileReference : UpscanFileReference, name : String, mimeType : String, downloadUrl : String, metadata : Map[String, String])

class Upscan @Inject()(callbackDispatcher : UpscanCallbackDispatcher, actorSystem : ActorSystem, implicit val ec : ExecutionContext) {

  def initiate(redirectOnSuccess : Option[String], metadataFields : Map[String, String]) : UpscanInitiateResponse = {

    val fileReference = UpscanFileReference(UUID.randomUUID().toString)

    val formFields = Map.empty[String,String] ++
      redirectOnSuccess.map("redirectOnSuccess" -> _) ++
      metadataFields.map(entry => s"x-amz-meta-${entry._1}" -> entry._2) +
      ("fileReference" -> fileReference.reference)


    UpscanInitiateResponse(
      fileReference = fileReference,
      postTarget = uk.gov.hmrc.upscan.controllers.routes.UpscanController.upload().url,
      formFields
    )

  }

  def handleUpload(fileReference : String, filename : String, mimeType : Option[String], file : play.api.libs.Files.TemporaryFile,
                   metadataFields: Map[String, String]): Unit = {

    val uploadedFile = UploadCallback(
      UpscanFileReference(fileReference),
      filename,
      mimeType.getOrElse("application/xml"), s"http://localhost:9000/${fileReference}",
      metadataFields)

    actorSystem.scheduler.scheduleOnce(3 seconds) {
      callbackDispatcher.handleCallback(uploadedFile)
    }

  }

}
