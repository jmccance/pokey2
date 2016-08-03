package pokey.user.model

import play.api.libs.json._
import pokey.test.UnitSpec

class UserSpec extends UnitSpec {

  "A User" when {
    "serialized to JSON" should {
      "contain the expected fields" in {
        val user = User("1234", "Antoine")
        val json = Json.toJson(user)

        (json \ "id") shouldBe JsDefined(JsString(user.id))
        (json \ "name") shouldBe JsDefined(JsString(user.name))
      }
    }
  }
}
