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

package uk.gov.hmrc.helloworldupscan.services

import uk.gov.hmrc.helloworldupscan.connectors.Reference
import uk.gov.hmrc.helloworldupscan.model.{UploadId, UploadStatus}

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator
import javax.inject.Singleton
import scala.concurrent.Future


@Singleton
class InMemoryUploadProgressTracker
  extends UploadProgressTracker:

  case class Entry(
    uploadId    : UploadId,
    reference   : Reference,
    uploadStatus: UploadStatus
  )

  private val entries: AtomicReference[Set[Entry]] =
    AtomicReference[Set[Entry]](Set.empty)

  def getUploadResult(id: UploadId): Future[Option[UploadStatus]] =
    Future.successful(entries.get.find(_.uploadId == id).map(_.uploadStatus))

  override def requestUpload(uploadId: UploadId, fileReference: Reference): Future[Unit] =
    entries.updateAndGet:
      (t: Set[Entry]) =>
        t.filterNot(in => in.uploadId == uploadId|| in.reference == fileReference)
          + Entry(uploadId, fileReference, UploadStatus.InProgress)
    Future.unit

  override def registerUploadResult(reference: Reference, uploadStatus: UploadStatus): Future[Unit] =
    entries.updateAndGet:
      (t: Set[Entry]) =>
        val existing = t.find(_.reference == reference).getOrElse(throw RuntimeException("Doesn't exist"))
        t - existing + existing.copy(uploadStatus = uploadStatus)
    Future.unit
