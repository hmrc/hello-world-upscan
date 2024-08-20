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

import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.helloworldupscan.connectors.Reference
import uk.gov.hmrc.helloworldupscan.model._
import uk.gov.hmrc.helloworldupscan.repository.UserSessionRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class MongoBackedUploadProgressTrackerSpec extends AnyWordSpec
  with Matchers
  with DefaultPlayMongoRepositorySupport[UploadDetails]
  with IntegrationPatience {

  override val repository: UserSessionRepository = new UserSessionRepository(mongoComponent)

  val t = new MongoBackedUploadProgressTracker(repository)

  "MongoBackedUploadProgressTracker" should {
    "coordinate workflow" in {
      val reference = Reference("reference")
      val id = UploadId("upload-id")
      val expectedStatus = UploadedSuccessfully("name", "mimeType", "downloadUrl", size = Some(123))

      t.requestUpload(id, reference).futureValue
      t.registerUploadResult(reference, expectedStatus).futureValue

      val result = t.getUploadResult(id).futureValue

      result shouldBe Some(expectedStatus)
    }
  }
}