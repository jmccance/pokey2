package pokey.user.model

import play.api.libs.json._
import pokey.test.UnitSpec

class UserSpec extends UnitSpec {

  "A User" when {
    "serialized to JSON" should {
      "contain the expected fields" in {
        val user = User(User.Id.unsafeFrom("1234"), User.Name.unsafeFrom("Antoine"))
        val json = Json.toJson(user)

        (json \ "id") shouldBe JsDefined(JsString(user.id.value))
        (json \ "name") shouldBe JsDefined(JsString(user.name.value))
      }
    }
  }

  "A User.Id" when {
    "serialized to JSON" should {
      "be serialized to its value as a JsString" in pending
    }

    "serialized as JSON from a Map[User.Id, A]" should {
      "serialize the keys of the map as the Ids' values" in pending
    }
  }
}
