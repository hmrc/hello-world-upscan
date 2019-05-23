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

package uk.gov.hmrc.helloworldupscan.connectors

import javax.inject.Inject
import play.api.libs.json.{Json, Reads}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.helloworldupscan.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.upscan.services.{UpscanFileReference, UpscanInitiateResponse}
import PreparedUpload._

import scala.concurrent.{ExecutionContext, Future}

case class UpscanInitiateRequest(callbackUrl: String, successRedirect : Option[String] = None, minimumFileSize: Option[Int] = None, maximumFileSize: Option[Int] = None)

case class UploadForm(href: String, fields: Map[String, String])

case class Reference(value: String) extends AnyVal

object Reference {
  implicit val referenceReader = Reads.StringReads.map(Reference(_))
}

case class PreparedUpload(reference: Reference, uploadRequest: UploadForm)

object UpscanInitiateRequest {
  implicit val format = Json.format[UpscanInitiateRequest]
}

object PreparedUpload {

  implicit val uploadFormFormat = Json.reads[UploadForm]

  implicit val format = Json.reads[PreparedUpload]
}

class UpscanInitiateConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec : ExecutionContext) {

  private val headers = Map(
    HeaderNames.USER_AGENT -> "upscan-acceptance-tests",
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  def initiate(redirectOnSuccess : Option[String])(implicit hc : HeaderCarrier) : Future[UpscanInitiateResponse] = {

    val request = UpscanInitiateRequest(appConfig.callbackEndpointTarget, redirectOnSuccess)

    for {
      response <- httpClient.POST[UpscanInitiateRequest, PreparedUpload](appConfig.initiateUrl, request, headers.toSeq)
      } yield UpscanInitiateResponse(
        UpscanFileReference(response.reference.value),
        response.uploadRequest.href,
        response.uploadRequest.fields
      )

  }

}
