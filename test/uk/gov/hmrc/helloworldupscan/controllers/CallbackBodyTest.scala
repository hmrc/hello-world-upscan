package uk.gov.hmrc.helloworldupscan.controllers

import java.net.URL
import java.time.Instant

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.helloworldupscan.connectors.Reference

class CallbackBodyTest extends WordSpec with Matchers {

  "CallbackBody JSON reader" should {
    "be able to deserialize successful body" in {

      val body =
        """
          |{
          |    "reference" : "11370e18-6e24-453e-b45a-76d3e32ea33d",
          |    "fileStatus" : "READY",
          |    "downloadUrl" : "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
          |    "uploadDetails": {
          |        "uploadTimestamp": "2018-04-24T09:30:00Z",
          |        "checksum": "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          |        "fileName": "test.pdf",
          |        "fileMimeType": "application/pdf"
          |    }
          |}
          |
        """.stripMargin

      CallbackBody.reads.reads(Json.parse(body)) shouldBe
        JsSuccess(
        ReadyCallbackBody(
          reference = Reference("11370e18-6e24-453e-b45a-76d3e32ea33d"),
          downloadUrl = new URL("https://bucketName.s3.eu-west-2.amazonaws.com?1235676"),
          uploadDetails = UploadDetails(
            uploadTimestamp = Instant.parse("2018-04-24T09:30:00Z"),
            checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
            fileMimeType = "application/pdf",
            fileName = "test.pdf"
          )
        )
        )

    }

    "should be able to deserialize failed body" in {

      val body =
        """
          |{
          |    "reference" : "11370e18-6e24-453e-b45a-76d3e32ea33d",
          |    "fileStatus" : "FAILED",
          |    "failureDetails": {
          |        "failureReason": "QUARANTINE",
          |        "message": "e.g. This file has a virus"
          |    }
          |}
        """.stripMargin

      CallbackBody.reads.reads(Json.parse(body)) shouldBe
      JsSuccess(
        FailedCallbackBody(
          reference = Reference("11370e18-6e24-453e-b45a-76d3e32ea33d"),
          failureDetails = ErrorDetails(
            failureReason = "QUARANTINE",
            message = "e.g. This file has a virus"
          )
        ))

    }
  }

}
