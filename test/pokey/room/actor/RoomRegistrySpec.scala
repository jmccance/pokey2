package pokey.room.actor

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.testkit.EventFilter
import akka.util.Timeout
import pokey.room.actor.RoomRegistry.{CreateRoomError, CreateRoomFor, GetRoomProxy}
import pokey.test.AkkaUnitSpec
import pokey.user.actor.UserProxy
import pokey.user.service.{StubUserService, UserService}
import pokey.util.using

import concurrent.duration._
import concurrent.{ExecutionContext, Future}

class RoomRegistrySpec extends AkkaUnitSpec {

  "A RoomRegistry" which {
    val roomId = "0"
    val invalidRoomId = "XX"
    val userId = "2222"
    val invalidUserId = "XXXX"

    "receives a GetRoomProxy" should {
      "reply with the existing RoomProxy if it exists" in {
        val registry = newRoomRegistry()
        registry ! GetRoomProxy(roomId)

        expectMsgPF() {
          case oProxy: Option[_] => oProxy should not be empty
        }
      }

      "reply with None if the room does not exist" in {
        val registry = newRoomRegistry()
        registry ! GetRoomProxy(invalidRoomId)

        expectMsgPF() {
          case oProxy: Option[_] => oProxy shouldBe empty
        }
      }
    }

    "receives a CreateRoomFor message" should {
      "reply with a newly created RoomProxy if the user exists" in {
        val registry = newRoomRegistry()
        registry ! CreateRoomFor(userId)

        expectMsgPF() {
          case msg => msg shouldBe a [RoomProxy]
        }
      }

      "reply with an error if the user does not exist" in {
        val registry = newRoomRegistry()
        registry ! CreateRoomFor(invalidUserId)

        expectMsgPF() {
          case msg => msg shouldBe a [CreateRoomError]
        }
      }

    }

    "receives a notification that a room has terminated" should {
      "remove that room from the map" in {
        implicit val timeout = Timeout(3.seconds)
        val registry = newRoomRegistry()
        
        val foRoomProxy = (registry ? GetRoomProxy(roomId)).mapTo[Option[RoomProxy]]

        whenReady(foRoomProxy) {
          case Some(roomProxy) =>
            EventFilter.info(start = "room_closed", occurrences = 1) intercept {
              system.stop(roomProxy.ref)
            }
            registry ! GetRoomProxy(roomId)
            expectMsg(None)
        }
      }
    }

    class TestRoomRegistry(userService: UserService)
      extends RoomRegistry(Stream.from(0).map(_.toString), userService) {
    }

    class TestUserService(users: (String, ActorRef)*)
      extends StubUserService {
      private[this] val _users = Map(userId -> system.deadLetters) ++ users

      override def getUser(id: String)
                          (implicit ec: ExecutionContext): Future[Option[UserProxy]] =
        Future.successful(_users.get(id).map(ref => UserProxy(id, ref)))
    }

    def newRoomRegistry(userService: UserService = new TestUserService): ActorRef =
      using(system.actorOf(Props(new TestRoomRegistry(userService)))) { registry =>
        registry ! CreateRoomFor(userId)
        expectMsgType[RoomProxy]
      }
  }
}
