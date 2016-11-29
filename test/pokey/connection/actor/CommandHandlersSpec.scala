package pokey.connection.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import play.api.libs.json.JsString
import pokey.connection.actor.CommandHandlers._
import pokey.connection.model.Commands._
import pokey.connection.model.Events.{ErrorEvent, RoomCreatedEvent}
import pokey.room.actor.{RoomProxy, RoomProxyActor}
import pokey.room.model.{Estimate, Room}
import pokey.room.service.StubRoomService
import pokey.test.AkkaUnitSpec
import pokey.user.actor.{UserProxy, UserProxyActor}
import pokey.user.model.User

import scala.concurrent.{ExecutionContext, Future}

class CommandHandlersSpec extends AkkaUnitSpec {
  "CommandHandlers" when {
    "handling a ClearRoomCommand" when {
      "the room is known" should {
        "send a ClearRoom message to the RoomProxy" in new Scenario {
          handler(ClearRoomCommand(ownedRoomId))
          ownedRoomP.expectMsg(RoomProxyActor.ClearFor(connUserId))
        }
      }

      "the room is not known" should {
        "send an ErrorEvent to the client" in new Scenario {
          handler(ClearRoomCommand(unknownRoomId))
          clientP.expectMsgType[ErrorEvent]
        }
      }
    }

    "handling a CreateRoomCommand" should {
      "create a room for the UserProxy" in new Scenario {
        handler(CreateRoomCommand)
        clientP.expectMsg(RoomCreatedEvent(newRoomId))
      }
    }

    "handling an InvalidCommand" should {
      "send an ErrorEvent to the client" in new Scenario {
        handler(InvalidCommand(JsString("lolwut")))
        clientP.expectMsgType[ErrorEvent]
      }
    }

    "handling a JoinRoomCommand" when {
      "the room exists" should {
        "send a JoinRoom message to the RoomProxy" in new Scenario {
          handler(JoinRoomCommand(unjoinedRoomId))
          unjoinedRoomProxyP.expectMsg(RoomProxyActor.JoinRoom(userProxy))
        }
      }

      "the room does not exist" should {
        "send an ErrorEvent to the client" in new Scenario {
          handler(JoinRoomCommand(unknownRoomId))
          clientP.expectMsgType[ErrorEvent]
        }
      }
    }

    "handling a KillConnectionCommand" should {
      "send a PoisonPill to the implicit sender" in new Scenario {
        clientP.watch(self)

        handler(KillConnectionCommand)
        clientP.expectTerminated(self)
      }
    }

    "handling a SetNameCommand" should {
      "send a SetName message to the UserProxy" in new Scenario {
        handler(SetNameCommand(newName))
        userProxyP.expectMsg(UserProxyActor.SetName(newName))
      }
    }

    "handling a RevealRoomCommand" when {
      "the room is known" should {
        "send a RevealRoom message to the RoomProxy" in new Scenario {
          handler(RevealRoomCommand(ownedRoomId))
          ownedRoomP.expectMsg(RoomProxyActor.RevealFor(connUserId))
        }
      }

      "the room is not known" should {
        "send an ErrorEvent to the client" in new Scenario {
          handler(RevealRoomCommand(unknownRoomId))
          clientP.expectMsgType[ErrorEvent]
        }
      }
    }

    "handling a SetTopicCommand" when {
      "the room is known" should {
        "send a SetTopic message to the RoomProxy" in new Scenario {
          val command = SetTopicCommand(ownedRoomId, someTopic)

          handler(command)
          ownedRoomP.expectMsg(RoomProxyActor.SetTopic(connUserId, command.topic))
        }
      }

      "the room does not exist" should {
        "send an ErrorEvent to the client" in new Scenario {
          handler(SetTopicCommand(unknownRoomId, someTopic))
          clientP.expectMsgType[ErrorEvent]
        }
      }
    }

    "handling a SubmitEstimateCommand" when {
      "the room exists" should {
        "send a SubmitEstimate message to the RoomProxy" in new Scenario {
          handler(SubmitEstimateCommand(ownedRoomId, someEstimate))
          ownedRoomP.expectMsg(RoomProxyActor.SubmitEstimate(connUserId, someEstimate))
        }
      }

      "the room does not exist" should {
        "send an ErrorEvent to the client" in new Scenario {
          handler(SubmitEstimateCommand(unknownRoomId, someEstimate))
          clientP.expectMsgType[ErrorEvent]
        }
      }
    }
  }

  class Scenario {
    implicit val ec = system.dispatcher

    val clientP = TestProbe()
    val ownedRoomP = TestProbe()
    val userProxyP = TestProbe()

    val connUserId = User.Id.unsafeFrom("conn-user-id-0000")
    val userProxy = UserProxy(connUserId, userProxyP.ref)

    val newName = User.Name.unsafeFrom("Magrat")

    val newRoomId = Room.Id.unsafeFrom("new-room-id-0000")
    val unknownRoomId = Room.Id.unsafeFrom("unknown-room-id-0000")
    val ownedRoomId = Room.Id.unsafeFrom("known-room-id-0000")
    val unjoinedRoomId = Room.Id.unsafeFrom("known-room-id-0000")

    val unjoinedRoomProxyP = TestProbe()

    val roomService = new TestRoomService(unjoinedRoomId -> unjoinedRoomProxyP.ref)

    val rooms = Map(ownedRoomId -> ownedRoomP.ref)

    val someEstimate = Estimate(Option("1"), Option("asdf"))
    val someTopic = "hot topic"

    val handler = handleCommandWith(clientP.ref, connUserId, rooms, roomService, userProxy)

    class TestRoomService(_rooms: (Room.Id, ActorRef)*) extends StubRoomService {
      private[this] val rooms = Map(_rooms: _*)

      // For the purposes of the current specs, this does not actually need to work.
      override def createRoom(ownerId: User.Id)(implicit ec: ExecutionContext): Future[RoomProxy] =
        Future.successful(RoomProxy(newRoomId, TestProbe().ref))

      override def getRoom(id: Room.Id)(implicit ec: ExecutionContext): Future[Option[RoomProxy]] =
        Future.successful(rooms.get(id).map(ref => RoomProxy(id, ref)))
    }
  }
}
