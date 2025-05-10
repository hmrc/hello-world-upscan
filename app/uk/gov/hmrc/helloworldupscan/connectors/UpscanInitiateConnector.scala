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

package uk.gov.hmrc.helloworldupscan.connectors

import play.api.libs.json.{Json, Format, Reads, Writes}
import play.api.libs.ws.writeableOf_JsValue
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.helloworldupscan.config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.upscan.services.{UpscanFileReference, UpscanInitiateResponse}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// TODO expectedContentType is also an optional value
case class UpscanInitiateRequest(
  callbackUrl    : String,
  successRedirect: Option[String] = None,
  errorRedirect  : Option[String] = None,
  minimumFileSize: Option[Int]    = None,
  maximumFileSize: Option[Int]    = Some(512)
)

object UpscanInitiateRequest:
  given Format[UpscanInitiateRequest] = Json.format[UpscanInitiateRequest]

case class UploadForm(
  href  : String,
  fields: Map[String, String]
)

case class Reference(value: String) extends AnyVal

object Reference:
  given Reads[Reference] = Reads.StringReads.map(Reference(_))

case class PreparedUpload(
  reference   : Reference,
  uploadRequest: UploadForm
)

object PreparedUpload:
  given Reads[UploadForm]     = Json.reads[UploadForm]
  given Reads[PreparedUpload] = Json.reads[PreparedUpload]

class UpscanInitiateConnector @Inject()(
  httpClient: HttpClientV2,
  appConfig : AppConfig
)(using ExecutionContext):

  private val headers = Map(
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  def initiate(
    redirectOnSuccess: Option[String],
    redirectOnError  : Option[String]
  )(using HeaderCarrier): Future[UpscanInitiateResponse] =
    val request = UpscanInitiateRequest(
      callbackUrl     = appConfig.callbackEndpointTarget,
      successRedirect = redirectOnSuccess,
      errorRedirect   = redirectOnError
    )

    for
      response      <- httpClient.post(url"${appConfig.initiateV2Url}")
                         .withBody(Json.toJson(request))
                         .setHeader(headers.toSeq: _*)
                         .execute[PreparedUpload]
      fileReference =  UpscanFileReference(response.reference.value)
      postTarget    =  response.uploadRequest.href
      formFields    =  response.uploadRequest.fields
    yield UpscanInitiateResponse(fileReference, postTarget, formFields)
