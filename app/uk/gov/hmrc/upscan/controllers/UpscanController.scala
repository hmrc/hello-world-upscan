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

package uk.gov.hmrc.upscan.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.http.HttpEntity
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.helloworldupscan.config.AppConfig
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.upscan.services.Upscan

import scala.concurrent.Future

@Singleton
class UpscanController @Inject()(val messagesApi: MessagesApi, val upscan : Upscan, implicit val appConfig: AppConfig) extends BaseController {

  case class UploadForm(redirectOnSuccess : Option[String])

  val userForm = Form(
    mapping(
      "redirectOnSuccess" -> optional(text)
    )(UploadForm.apply)(UploadForm  .unapply)
  )

  val upload = Action.async(parse.multipartFormData) { implicit request =>

    request.body.file("file").map { file =>

      userForm.bindFromRequest().fold(
        errors => {
          Future.successful(BadRequest("Problem with a form"))
        },
        form => {
          val filename = file.filename
          val contentType = file.contentType
          upscan.handleUpload(filename, file.ref)
          Future.successful(form.redirectOnSuccess.fold(NoContent)(Redirect(_)))
        }
      )

    }.getOrElse {
      Future.successful(BadRequest("No file found"))
    }

  }


}
