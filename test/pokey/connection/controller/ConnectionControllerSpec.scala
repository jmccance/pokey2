package pokey.connection.controller

import akka.actor.{ Actor, ActorRef, Props }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import pokey.connection.actor.ConnectionHandler
import pokey.test.PlayUnitSpec
import pokey.user.actor.UserProxy
import pokey.user.service.UserService
import scaldi.play.ScaldiApplicationBuilder._

class ConnectionControllerSpec extends PlayUnitSpec {
  "A ConnectionController" when {
    "the client connects with a user id" should {
      "establish a WebSocket connection" in pending
    }

    "the client connects without a valid user id" should {
      "return an Unauthorized error" in withScaldiApp() {
        val controller = newController()
        val result = controller.connect.f(FakeRequest()).map(_.left.get)
        status(result) mustBe UNAUTHORIZED
      }
    }

    def newController() = {
      val mockService = mock[UserService]
      val propsFactory: ConnectionHandler.PropsFactory =
        (userProxy: UserProxy) =>
          (actor: ActorRef) =>
            Props(new Actor { def receive = Actor.emptyBehavior })
      new ConnectionController(mockService, propsFactory)
    }
  }
}
