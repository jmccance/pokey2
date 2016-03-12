package pokey.connection.actor

import akka.actor.{Actor, ActorLogging}
import akka.testkit.TestProbe
import pokey.connection.actor.CommandHandlers._
import pokey.connection.model.Commands.SetTopicCommand
import pokey.connection.model.Events.ErrorEvent
import pokey.room.actor.RoomProxyActor
import pokey.test.AkkaUnitSpec

class CommandHandlersSpec extends AkkaUnitSpec {
  "CommandHandlers" when {
    "handling a SetTopicCommand" when {
      "the room is known" should {
        "send a SetTopic message to the RoomProxy" in new Scenario {
          val handler = setTopicCommandHandler(clientP.ref, connUserId, rooms)
          val command = SetTopicCommand(someOwnedRoomId, someTopic)

          handler(command)
          ownedRoomP.expectMsg(RoomProxyActor.SetTopic(connUserId, command.topic))
        }
      }

      "the room does not exist" should {
        "reply with an ErrorEvent" in new Scenario {
          val handler = setTopicCommandHandler(clientP.ref, connUserId, rooms)

          handler(SetTopicCommand(unknownRoomId, someTopic))
          clientP.expectMsgType[ErrorEvent]
        }
      }
    }
  }

  class TestCommandHandler extends Actor with ActorLogging with CommandHandlers {
    def receive = Actor.emptyBehavior
  }

  class Scenario {
    val clientP = TestProbe()
    val ownedRoomP = TestProbe()

    val connUserId = "conn-user-id-0000"

    val unknownRoomId = "unknown-room-id-0000"
    val someOwnedRoomId = "known-room-id-0000"

    val rooms = Map(someOwnedRoomId -> ownedRoomP.ref)

    val someTopic = "hot topic"
  }
}
