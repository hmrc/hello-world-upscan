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

package uk.gov.hmrc.helloworldupscan.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.mvc.*
import uk.gov.hmrc.helloworldupscan.config.AppConfig
import uk.gov.hmrc.helloworldupscan.connectors.{Reference, UpscanInitiateConnector}
import uk.gov.hmrc.helloworldupscan.model.{UploadId, UploadStatus}
import uk.gov.hmrc.helloworldupscan.services.UploadProgressTracker
import uk.gov.hmrc.helloworldupscan.views
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UploadFormController @Inject()(
  upscanInitiateConnector: UpscanInitiateConnector,
  uploadProgressTracker  : UploadProgressTracker,
  mcc                    : MessagesControllerComponents,
  uploadFormView         : views.html.UploadForm,
  uploadResultView       : views.html.UploadResult,
  submissionFormView     : views.html.SubmissionForm,
  errorView              : views.html.ErrorTemplate,
  submissionResultView   : views.html.SubmissionResult
)(using
  appConfig: AppConfig,
  ec       : ExecutionContext
) extends FrontendController(mcc)
     with Logging:

  val show: Action[AnyContent] =
    Action.async: request =>
      given MessagesRequestHeader = request
      val uploadId           = UploadId.generate()
      val successRedirectUrl = appConfig.uploadRedirectTargetBase + routes.UploadFormController.showResult(uploadId).url
      val errorRedirectUrl   = appConfig.uploadRedirectTargetBase + "/hello-world-upscan/hello-world/error"
      for
        upscanInitiateResponse <- upscanInitiateConnector.initiate(Some(successRedirectUrl), Some(errorRedirectUrl))
        _                      <- uploadProgressTracker.requestUpload(uploadId, Reference(upscanInitiateResponse.fileReference.reference))
      yield Ok(uploadFormView(upscanInitiateResponse))

  def showResult(uploadId: UploadId): Action[AnyContent] =
    Action.async: request =>
      given MessagesRequestHeader = request
      for uploadResult <- uploadProgressTracker.getUploadResult(uploadId) yield
        uploadResult match
          case Some(result) => Ok(uploadResultView(uploadId, result))
          case None         => BadRequest(s"Upload with id $uploadId not found")

  def showError(
    errorCode     : String,
    errorMessage  : String,
    errorRequestId: String,
    key           : String
  ): Action[AnyContent] =
    Action: request =>
      given MessagesRequestHeader = request
      Ok(errorView(
        "Upload Error",
        errorMessage,
        s"Code: $errorCode, RequestId: $errorRequestId, FileReference: $key"
      ))

  private case class SampleForm(
    field1        : String,
    field2        : String,
    uploadedFileId: UploadId
  )

  private val sampleForm =
    Form(
      mapping(
        "field1"         -> text,
        "field2"         -> text,
        "uploadedFileId" -> text.transform[UploadId](UploadId(_), _.value)
      )(SampleForm.apply)(o => Some(Tuple.fromProductTyped(o)))
    )

  def showSubmissionForm(uploadId: UploadId): Action[AnyContent] =
    Action.async: request =>
      given MessagesRequest[AnyContent] = request
      val emptyForm = sampleForm.fill(SampleForm("", "", uploadId))
      uploadProgressTracker.getUploadResult(uploadId)
        .map:
          case Some(s: UploadStatus.UploadedSuccessfully) => Ok(submissionFormView(emptyForm, s))
          case _                                          => InternalServerError("Something gone wrong")

  def submitFormWithFile(): Action[AnyContent] =
    Action.async: request =>
      given Request[AnyContent] = request
      sampleForm
        .bindFromRequest()
        .fold(
          errors => {
            Future.successful(BadRequest(s"Problem with a form $errors"))
          },
          _ => {
            logger.info("Form successfully submitted")
            Future.successful(Redirect(routes.UploadFormController.showSubmissionResult()))
          }
        )

  def showSubmissionResult(): Action[AnyContent] =
    Action.async: request =>
      given MessagesRequestHeader = request
      Future.successful(Ok(submissionResultView()))
