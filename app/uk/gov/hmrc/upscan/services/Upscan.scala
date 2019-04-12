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
import uk.gov.hmrc.helloworldupscan.services.{UploadedFile, UpscanCallbackDispatcher}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class UpscanFileReference(reference : String)

case class UpscanInitiateResponse(fileReference: UpscanFileReference, postTarget : String, formFields : Map[String, String])

class Upscan @Inject()(callbackDispatcher : UpscanCallbackDispatcher, actorSystem : ActorSystem, implicit val ec : ExecutionContext) {

  def initiate(redirectOnSuccess : Option[String]) : UpscanInitiateResponse = {

    val formFields = Map.empty[String,String] ++ redirectOnSuccess.map("redirectOnSuccess" -> _)

    UpscanInitiateResponse(
      fileReference = UpscanFileReference(UUID.randomUUID().toString),
      postTarget = uk.gov.hmrc.upscan.controllers.routes.UpscanController.upload().url,
      formFields
    )

  }

  def handleUpload(filename : String, file : play.api.libs.Files.TemporaryFile): Unit = {

    val uploadedFile = UploadedFile("XX", filename)

    actorSystem.scheduler.scheduleOnce(3 seconds) {
      callbackDispatcher.handleCallback(uploadedFile)
    }

  }

}
