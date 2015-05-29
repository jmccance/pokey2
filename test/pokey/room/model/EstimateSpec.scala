package pokey.room.model

import play.api.libs.json._
import pokey.test.UnitSpec

class EstimateSpec extends UnitSpec {
  val estimates = Seq(
    Estimate(None, None),
    Estimate(None, Option("No idea")),
    Estimate(Option("5"), None),
    Estimate(Option("5"), Option("Give or take"))
  )

  "An Estimate" when {
    "converted to a hidden estimate" should {
      "be hidden" in {
        val hiddenEstimates = estimates.map(_.asHidden)

        hiddenEstimates should contain only HiddenEstimate
      }
    }

    "converted to a revealed estimate" should {
      "be revealed" in {
        val revealedEstimates = estimates.map(_.asRevealed)

        revealedEstimates should contain theSameElementsInOrderAs estimates.map {
          case Estimate(value, comment) => RevealedEstimate(value, comment)
        }
      }
    }

    "being deserialized from JSON" should {

      "deserialize from JSON correctly when both value and comment are present" in {
        val json =
          """
            |{
            |  "value": "XXS",
            |  "comment": "Already done"
            |}
          """.stripMargin

        parseEstimate(json).value shouldBe Estimate(Some("XXS"), Some("Already done"))
      }

      "deserialize from JSON correctly when only the value is present" in {
        val json =
          """
            |{
            |  "value": "XXS"
            |}
          """.stripMargin

        parseEstimate(json).value shouldBe Estimate(Some("XXS"), None)
      }

      "deserialize from JSON correctly when only the comment is present" in {
        val json =
          """
            |{
            |  "comment": "Not my job."
            |}
          """.stripMargin

        parseEstimate(json).value shouldBe Estimate(None, Some("Not my job."))
      }

      "deserialize from JSON correctly when neither of the estimate fields are present" in {
        val json =
          """
            |{
            |}
          """.stripMargin

        parseEstimate(json).value shouldBe Estimate(None, None)
      }
    }

    def parseEstimate(json: String) = Json.parse(json).asOpt[Estimate]
  }

  "A HiddenEstimate" when {
    "serialized to JSON" should {
      "be an empty JSON object" in {
        Json.toJson(HiddenEstimate) shouldBe Json.obj()
      }
    }
  }

  "A RevealedEstimate" when {
    "serialized to JSON" should {
      "represent the revealed estimate" in {
        val revealedEstimates: Seq[RevealedEstimate] = estimates.map(_.asRevealed)
        val revealedEstimatesJson = revealedEstimates.map(Json.toJson(_))

        forAll(revealedEstimatesJson.zip(revealedEstimates)) {
          case (json, estimate) =>
            val expectedValue = estimate.value.map(JsString).getOrElse(JsNull)
            val expectedComment = estimate.comment.map(JsString).getOrElse(JsNull)
            (json \ "value") shouldBe JsDefined(expectedValue)
            (json \ "comment") shouldBe JsDefined(expectedComment)
        }
      }
    }
  }

}
