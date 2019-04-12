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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.helloworldupscan.config.AppConfig
import uk.gov.hmrc.helloworldupscan.services.SessionStorage
import uk.gov.hmrc.helloworldupscan.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.upscan.services.Upscan

import scala.concurrent.Future

@Singleton
class UploadFormController @Inject()(val messagesApi: MessagesApi, val upscan : Upscan, val sessionStorage : SessionStorage, implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val show = Action.async { implicit request =>
    val upscanInitiateResponse = upscan.initiate(
      redirectOnSuccess = Some(uk.gov.hmrc.helloworldupscan.controllers.routes.UploadFormController.showResult().url)
    )

    Future.successful(Ok(views.html.upload_form(upscanInitiateResponse)))
  }

  val showResult = Action.async {
    implicit request =>
      val uploadResult = sessionStorage.getUploadResult
      Future.successful(Ok(views.html.upload_result(uploadResult)))
  }

}
