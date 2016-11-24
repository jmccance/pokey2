package pokey.connection.controller

import play.api.libs.concurrent.Execution.Implicits._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import pokey.test.PlayUnitSpec
import scaldi.Injectable
import scaldi.play.ScaldiApplicationBuilder._

class ConnectionControllerSpec extends PlayUnitSpec {
  "A ConnectionController" when {
    "the client connects with a user id" should {
      "establish a WebSocket connection" in pending
    }

    "the client connects without a valid user id" should {
      "return an Unauthorized error" in withScaldiInj() { implicit injector =>
        val controller = Injectable.inject[ConnectionController]
        val result = controller.connect(FakeRequest()).map(_.left.get)
        status(result) mustBe UNAUTHORIZED
      }
    }
  }
}
