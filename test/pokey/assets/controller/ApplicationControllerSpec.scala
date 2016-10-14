package pokey.assets.controller

import org.mockito.Mockito._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import pokey.application.ApplicationController
import pokey.test.PlayUnitSpec
import pokey.user.service.UserService
import scaldi.play.ScaldiApplicationBuilder._

class ApplicationControllerSpec extends PlayUnitSpec {
  "An AssetController" when {
    "the user_id is not defined" should {
      "add the user_id to the session" in withScaldiApp() {
        val (controller, _) = init()

        val result = controller.assets("/", "")(FakeRequest())
        session(result).get("user_id") must not be empty
      }
    }

    "the user_id is defined" should {
      "not the replace the existing user_id" in withScaldiApp() {
        val (controller, _) = init()
        val userId = "1234"

        val result = controller.assets("/", "") {
          FakeRequest().withSession("user_id" -> userId)
        }
        session(result).get("user_id").value mustBe userId
      }
    }

    def init(): (ApplicationController, UserService) = {
      val settings = ApplicationController.Settings(None)
      val userService = mock[UserService]
      when(userService.nextUserId()).thenReturn("asdf")
      (new ApplicationController(settings, userService), userService)
    }
  }
}
