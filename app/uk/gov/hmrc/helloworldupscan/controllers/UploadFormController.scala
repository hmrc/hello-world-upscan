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

package uk.gov.hmrc.helloworldupscan.controllers


import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.helloworldupscan.config.AppConfig
import uk.gov.hmrc.helloworldupscan.services.{UploadId, UploadProgressTracker, UploadedSuccessfully}
import uk.gov.hmrc.helloworldupscan.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.upscan.services.Upscan

import scala.concurrent.Future

@Singleton
class UploadFormController @Inject()(val messagesApi: MessagesApi, val upscan : Upscan, val uploadProgressTracker : UploadProgressTracker, implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val show = Action.async { implicit request =>

    val uploadId = uploadProgressTracker.requestUpload

    val upscanInitiateResponse = upscan.initiate(
      redirectOnSuccess = Some(uk.gov.hmrc.helloworldupscan.controllers.routes.UploadFormController.showResult(uploadId).url),
      metadataFields = Map(UploadProgressTracker.METADATA_UPLOAD_ID -> uploadId.value)
    )

    Future.successful(Ok(views.html.upload_form(upscanInitiateResponse)))
  }

  def showResult(uploadId : UploadId) = Action.async {
    implicit request =>
      val uploadResult = uploadProgressTracker.getUploadResult(uploadId)
      Future.successful(Ok(views.html.upload_result(uploadId, uploadResult)))
  }

  case class SampleForm(
                         field1 : String,
                         field2 : String,
                         uploadedFileId : UploadId)

  val sampleForm = Form(
    mapping(
      "field1" -> text,
          "field2" -> text,
      "uploadedFileId" -> text.transform[UploadId](UploadId(_),_.value)
    )(SampleForm.apply)(SampleForm.unapply)
  )

  def showSubmissionForm(uploadId: UploadId) = Action.async { implicit request =>
    val emptyForm = sampleForm.fill(SampleForm("", "", uploadId))
    uploadProgressTracker.getUploadResult(uploadId) match {
      case s : UploadedSuccessfully => Future.successful(Ok(views.html.submission_form(emptyForm, s)))
      case _ => Future.successful(InternalServerError("Something gone wrong"))
    }
  }

  def submitFormWithFile() = Action.async { implicit request =>
    sampleForm.bindFromRequest().fold(
      errors => {
        Future.successful(BadRequest(s"Problem with a form $errors"))
      },
      form => {
        Logger.info("Form successfully submitted")
        Future.successful(Redirect(uk.gov.hmrc.helloworldupscan.controllers.routes.UploadFormController.showSubmissionResult()))
      }
    )

  }

  def showSubmissionResult() = Action.async { implicit request =>
    Future.successful(Ok(views.html.submission_result()))
  }

}
