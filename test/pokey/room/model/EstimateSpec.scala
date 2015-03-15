package pokey.room.model

import play.api.libs.json.{JsNull, JsString, Json}
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
  }

  "A HiddenEstimate" when {
    "serialized to JSON" should {
      "be an empty JSON object" in {
        val estimate: PublicEstimate = HiddenEstimate
        Json.toJson(HiddenEstimate) shouldBe Json.obj()
      }
    }
  }

  "A RevealedEstimate" when {
    "serialized to JSON" should {
      "be an empty JSON object" in {
        val revealedEstimates: Seq[PublicEstimate] = estimates.map(_.asRevealed)
        val revealedEstimatesJson = revealedEstimates.map(Json.toJson(_))

        forAll (revealedEstimatesJson.zip(revealedEstimates)) {
          case (json, estimate: RevealedEstimate) =>
            val expectedValue = estimate.value.map(JsString).getOrElse(JsNull)
            val expectedComment = estimate.comment.map(JsString).getOrElse(JsNull)
            (json \ "value") shouldBe expectedValue
            (json \ "comment") shouldBe expectedComment
        }
      }
    }
  }

}
