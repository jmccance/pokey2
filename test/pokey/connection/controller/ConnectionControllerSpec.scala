package pokey.connection.controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import pokey.connection.actor.ConnectionHandler
import pokey.room.service.RoomService
import pokey.test.PlayUnitSpec
import pokey.user.service.UserService

import scala.concurrent.ExecutionContext.Implicits.global

class ConnectionControllerSpec extends PlayUnitSpec {
  "A ConnectionController" when {
    "the client connects with a user id" should {
      "establish a WebSocket connection" in pending
    }

    "the client connects without a valid user id" should {
      "return an Unauthorized error" in {
        val mockUserService = mock[UserService]
        val controller = new ConnectionController(
          Helpers.stubControllerComponents(),
          mockUserService,
          ConnectionHandler.propsFactory(mock[RoomService], mock[ConnectionHandler.Settings])
        )(mock[ActorSystem], mock[Materializer])
        val result = controller.connect(FakeRequest()).map(_.left.get)
        status(result) mustBe UNAUTHORIZED
      }
    }
  }
}