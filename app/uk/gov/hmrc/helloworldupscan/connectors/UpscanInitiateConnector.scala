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

sealed trait UpscanInitiateRequest

// TODO expectedContentType is also an optional value
case class UpscanInitiateRequestV1(
  callbackUrl    : String,
  successRedirect: Option[String] = None,
  minimumFileSize: Option[Int]    = None,
  maximumFileSize: Option[Int]    = Some(512)
) extends UpscanInitiateRequest

// TODO expectedContentType is also an optional value
case class UpscanInitiateRequestV2(
  callbackUrl    : String,
  successRedirect: Option[String] = None,
  errorRedirect  : Option[String] = None,
  minimumFileSize: Option[Int]    = None,
  maximumFileSize: Option[Int]    = Some(512)
) extends UpscanInitiateRequest

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

object UpscanInitiateRequestV1:
  given Format[UpscanInitiateRequestV1] = Json.format[UpscanInitiateRequestV1]

object UpscanInitiateRequestV2:
  given Format[UpscanInitiateRequestV2] = Json.format[UpscanInitiateRequestV2]

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

  def initiateV1(redirectOnSuccess: Option[String])(using HeaderCarrier): Future[UpscanInitiateResponse] =
    val request = UpscanInitiateRequestV1(
      callbackUrl     = appConfig.callbackEndpointTarget,
      successRedirect = redirectOnSuccess
    )
    initiate(appConfig.initiateUrl, request)

  def initiateV2(
    redirectOnSuccess: Option[String],
    redirectOnError  : Option[String]
  )(using HeaderCarrier): Future[UpscanInitiateResponse] =
    val request = UpscanInitiateRequestV2(
      callbackUrl     = appConfig.callbackEndpointTarget,
      successRedirect = redirectOnSuccess,
      errorRedirect   = redirectOnError
    )
    initiate(appConfig.initiateV2Url, request)

  private def initiate[T](
    url    : String,
    request: T
  )(using HeaderCarrier, Writes[T]): Future[UpscanInitiateResponse] =
    for
      response      <- httpClient.post(url"$url")
                         .withBody(Json.toJson(request))
                         .setHeader(headers.toSeq: _*)
                         .execute[PreparedUpload]
      fileReference =  UpscanFileReference(response.reference.value)
      postTarget    =  response.uploadRequest.href
      formFields    =  response.uploadRequest.fields
    yield UpscanInitiateResponse(fileReference, postTarget, formFields)
