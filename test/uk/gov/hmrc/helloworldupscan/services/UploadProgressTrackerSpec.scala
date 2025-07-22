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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.helloworldupscan.connectors.Reference
import uk.gov.hmrc.helloworldupscan.model.*
import uk.gov.hmrc.helloworldupscan.repository.UserSessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.objectstore.client.{Md5Hash, ObjectSummaryWithMd5, Path, RetentionPeriod, Sha256Checksum}
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import java.net.URL
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UploadProgressTrackerSpec
  extends AnyWordSpec
     with Matchers
     with DefaultPlayMongoRepositorySupport[UploadDetails]
     with IntegrationPatience:

  override val repository: UserSessionRepository = UserSessionRepository(mongoComponent)

  given HeaderCarrier = mock[HeaderCarrier]

  val objectStoreClient = mock[PlayObjectStoreClient]
  val progressTracker = UploadProgressTracker(repository, objectStoreClient)

  "UploadProgressTracker" should:
    "coordinate workflow" in:
      val reference = Reference("reference")
      val id = UploadId("upload-id")
      val downloadUrl = url"https://www.some-site.com/a-file.txt"
      val expectedStatus = UploadStatus.UploadedSuccessfully("name", "mimeType", downloadUrl, size = Some(123), checksum = "a142ed16d596494528e264ffdd5bfbd1188243e0ed1afc8768bcd5d76eb9c4f1")

      when(
        objectStoreClient.uploadFromUrl(
          from            = any[URL],
          to              = any[Path.File],
          retentionPeriod = any[RetentionPeriod],
          contentType     = any[Option[String]],
          contentMd5      = any[Option[Md5Hash]],
          contentSha256   = any[Option[Sha256Checksum]],
          owner           = any[String]
        )(using any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          ObjectSummaryWithMd5(
            location = Path.File("/some/file.txt"),
            contentLength = 100,
            contentMd5 = Md5Hash("md5hash"),
            lastModified = Instant.now()
          )
        )
      )

      progressTracker.requestUpload(id, reference).futureValue
      progressTracker.registerUploadResult(reference, expectedStatus).futureValue

      val result = progressTracker.getUploadResult(id).futureValue

      result shouldBe Some(expectedStatus)
