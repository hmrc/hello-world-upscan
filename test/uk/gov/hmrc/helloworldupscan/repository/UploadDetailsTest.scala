package uk.gov.hmrc.helloworldupscan.repository

import org.scalatest.{Matchers, WordSpec}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.helloworldupscan.model.{Failed, InProgress, UploadId, UploadedSuccessfully}

class UploadDetailsTest extends WordSpec with Matchers {

  "Serialization and deserialization of UploadDetails" should {

    "serialize and deserialize InProgress status" in {

      val input = UploadDetails(BSONObjectID.generate(), UploadId.generate, InProgress)

      val serialized = UploadDetails.format.writes(input)

      val output = UploadDetails.format.reads(serialized)

      output.get shouldBe input

    }

    "serialize and deserialize Failed status" in {

      val input = UploadDetails(BSONObjectID.generate(), UploadId.generate, Failed)

      val serialized = UploadDetails.format.writes(input)

      val output = UploadDetails.format.reads(serialized)

      output.get shouldBe input

    }

    "serialize and deserialize UploadedSuccessfully status" in {

      val input = UploadDetails(BSONObjectID.generate(), UploadId.generate, UploadedSuccessfully("foo.txt", "text/plain", "http:localhost:8080"))

      val serialized = UploadDetails.format.writes(input)

      val output = UploadDetails.format.reads(serialized)

      output.get shouldBe input

    }


  }

}
