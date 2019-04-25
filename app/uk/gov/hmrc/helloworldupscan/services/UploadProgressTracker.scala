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

package uk.gov.hmrc.helloworldupscan.services

import java.util.UUID

import javax.inject.Singleton
import play.api.mvc.QueryStringBindable

import scala.collection.concurrent.TrieMap

case class UploadId(value : String) extends AnyVal

object UploadId {
  def generate = UploadId(UUID.randomUUID().toString)

  implicit def queryBinder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[UploadId] =
    stringBinder.transform(UploadId(_),_.value)
}

sealed trait UploadStatus
case object InProgress extends UploadStatus
case object Failed extends UploadStatus
case class UploadedSuccessfully(name : String, mimeType : String, downloadUrl : String) extends UploadStatus

@Singleton
class UploadProgressTracker {

  val uploadsInProgress: scala.collection.concurrent.Map[UploadId, UploadStatus] = TrieMap[UploadId, UploadStatus]()

   def requestUpload: UploadId = {
    val uploadId = UploadId.generate
    uploadsInProgress.putIfAbsent(uploadId, InProgress)
    uploadId
  }

  def registerUploadResult(uploadId : UploadId, uploadStatus : UploadStatus): Unit = {
    uploadsInProgress.put(uploadId, uploadStatus)
  }

  def getUploadResult(id : UploadId): UploadStatus = {
    uploadsInProgress.getOrElse(id, throw new RuntimeException(s"Unexpected upload $id"))
  }

}

object UploadProgressTracker {
  val METADATA_UPLOAD_ID = "upload-id"
}