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

import javax.inject.Singleton
import uk.gov.hmrc.helloworldupscan.model.{InProgress, UploadId, UploadStatus}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future


@Singleton
class InMemoryUploadProgressTracker extends UploadProgressTracker {

  val uploadsInProgress: scala.collection.concurrent.Map[UploadId, UploadStatus] = TrieMap[UploadId, UploadStatus]()

   def requestUpload: Future[UploadId] = {
    val uploadId = UploadId.generate
    uploadsInProgress.putIfAbsent(uploadId, InProgress)
    Future.successful(uploadId)
  }

  def registerUploadResult(uploadId : UploadId, uploadStatus : UploadStatus): Future[Unit] = {
    uploadsInProgress.put(uploadId, uploadStatus)
    Future.successful(())
  }

  def getUploadResult(id : UploadId): Future[Option[UploadStatus]] = Future.successful(uploadsInProgress.get(id))

}

object InMemoryUploadProgressTracker {
  val METADATA_UPLOAD_ID = "upload-id"
}