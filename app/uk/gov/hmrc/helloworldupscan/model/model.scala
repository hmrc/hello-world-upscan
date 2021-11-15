/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.helloworldupscan.model

import play.api.mvc.PathBindable
import uk.gov.hmrc.play.binders.SimpleObjectBinder

import java.util.UUID

sealed trait UploadStatus

case object InProgress extends UploadStatus

case object Failed extends UploadStatus

case class UploadedSuccessfully(name: String, mimeType: String, downloadUrl: String, size: Option[Long]) extends UploadStatus

case class UploadId(value: String) extends AnyVal

object UploadId {
  def generate = UploadId(UUID.randomUUID().toString)

  implicit val binder: PathBindable[UploadId] =
    new SimpleObjectBinder[UploadId](UploadId.apply, _.value)
}
