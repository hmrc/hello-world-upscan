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
import uk.gov.hmrc.helloworldupscan.connectors.{Reference, UpscanInitiateConnector}
import uk.gov.hmrc.helloworldupscan.controllers.routes.UploadFormController
import uk.gov.hmrc.helloworldupscan.model.{UploadId, UploadedSuccessfully}
import uk.gov.hmrc.helloworldupscan.services.UploadProgressTracker
import uk.gov.hmrc.helloworldupscan.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class UploadFormController @Inject()(
  val messagesApi: MessagesApi,
  val upscanInitiateConnector: UpscanInitiateConnector,
  val uploadProgressTracker: UploadProgressTracker,
  implicit val appConfig: AppConfig)
    extends FrontendController
    with I18nSupport {

  val show: Action[AnyContent] = Action.async { implicit request =>
    val uploadId           = UploadId.generate
    val successRedirectUrl = appConfig.uploadRedirectTargetBase + UploadFormController.showResult(uploadId).url
    for {
      upscanInitiateResponse <- upscanInitiateConnector.initiateV1(Some(successRedirectUrl))
      _                      <- uploadProgressTracker.requestUpload(uploadId, Reference(upscanInitiateResponse.fileReference.reference))
    } yield Ok(views.html.upload_form(upscanInitiateResponse))
  }

  val showV2: Action[AnyContent] = Action.async { implicit request =>
    val uploadId           = UploadId.generate
    val successRedirectUrl = appConfig.uploadRedirectTargetBase + UploadFormController.showResult(uploadId).url
    val errorRedirectUrl   = appConfig.uploadRedirectTargetBase + "/hello-world-upscan/hello-world/error"
    for {
      upscanInitiateResponse <- upscanInitiateConnector.initiateV2(successRedirectUrl, errorRedirectUrl)
      _                      <- uploadProgressTracker.requestUpload(uploadId, Reference(upscanInitiateResponse.fileReference.reference))
    } yield Ok(views.html.upload_form(upscanInitiateResponse))
  }

  def showResult(uploadId: UploadId): Action[AnyContent] = Action.async { implicit request =>
    for (uploadResult <- uploadProgressTracker.getUploadResult(uploadId)) yield {
      uploadResult match {
        case Some(result) => Ok(views.html.upload_result(uploadId, result))
        case None         => BadRequest(s"Upload with id $uploadId not found")
      }
    }
  }

  def showError(
    errorCode: String,
    errorMessage: String,
    errorResource: String,
    errorRequestId: String): Action[AnyContent] = Action { implicit request =>
    Ok(
      views.html.error_template(
        "Upload Error",
        errorMessage,
        s"Code: $errorCode, Resource: $errorResource, RequestId: $errorRequestId"))
  }

  case class SampleForm(field1: String, field2: String, uploadedFileId: UploadId)

  val sampleForm = Form(
    mapping(
      "field1"         -> text,
      "field2"         -> text,
      "uploadedFileId" -> text.transform[UploadId](UploadId(_), _.value)
    )(SampleForm.apply)(SampleForm.unapply)
  )

  def showSubmissionForm(uploadId: UploadId): Action[AnyContent] = Action.async { implicit request =>
    val emptyForm = sampleForm.fill(SampleForm("", "", uploadId))
    for (uploadResult <- uploadProgressTracker.getUploadResult(uploadId)) yield {
      uploadResult match {
        case Some(s: UploadedSuccessfully) => Ok(views.html.submission_form(emptyForm, s))
        case _                             => InternalServerError("Something gone wrong")
      }
    }
  }

  def submitFormWithFile(): Action[AnyContent] = Action.async { implicit request =>
    sampleForm
      .bindFromRequest()
      .fold(
        errors => {
          Future.successful(BadRequest(s"Problem with a form $errors"))
        },
        _ => {
          Logger.info("Form successfully submitted")
          Future.successful(Redirect(UploadFormController.showSubmissionResult()))
        }
      )
  }

  def showSubmissionResult(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.submission_result()))
  }
}
