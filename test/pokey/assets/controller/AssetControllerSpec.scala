package pokey.assets.controller

import org.mockito.Mockito._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import pokey.test.PlayUnitSpec
import pokey.user.service.UserService

class AssetControllerSpec extends PlayUnitSpec {
  "An AssetController" when {
    "the user_id is not defined" should {
      "add the user_id to the session" in {
        val (controller, userService) = init()

        val result = controller.assets("/", "")(FakeRequest())
        session(result).get("user_id") must not be empty
      }
    }

    "the user_id is defined" should {
      "not the replace the existing user_id" in {
        val (controller, _) = init()
        val userId = "1234"

        val result = controller.assets("/", "") {
          FakeRequest().withSession("user_id" -> userId)
        }
        session(result).get("user_id").value mustBe userId
      }
    }

    def init(): (AssetController, UserService) = {
      val userService = mock[UserService]
      when(userService.nextUserId()).thenReturn("asdf")
      (new AssetController(userService), userService)
    }
  }
}
