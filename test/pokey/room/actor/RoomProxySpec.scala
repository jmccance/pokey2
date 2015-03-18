package pokey.room.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import pokey.room.actor.RoomProxyActor.{Closed, JoinRoom, RoomUpdated}
import pokey.room.model.Room
import pokey.test.AkkaUnitSpec
import pokey.user.actor.UserProxy
import pokey.user.model.User

class RoomProxySpec extends AkkaUnitSpec {
  val owner = User("U-1", "Esme")
  val roomId = "R-1"
  val room = Room("R-1", owner.id)

  "A RoomProxy" when {
    "it receives a JoinRoom message from a connection" should {
      "subscribe to the supplied UserProxy" in pending

      "subscribe the connection to itself" in pending

      "publish a UserJoined message to the room" in pending
    }

    "it receives a UserUpdated message for a member of the room" should {
      "publish the update to the room" in pending
    }

    "it receives a LeaveRoom message" which {
      "has a UserProxy that is a member" should {
        "unsubscribe from the UserProxy" in pending

        "unsubscribe the connection from itself" in pending

        "publish UserLeft message to the room" in pending
      }
    }

    "it receives a SubmitEstimate message from a connection" should {
      "send an EstimateUpdated out to members of the room" in pending
    }

    "it receives a RevealFor message for a userId" which {
      "is allowed to reveal the room" should {
        "send a Revealed message to the room" in pending
      }

      "is not allowed to reveal the room" should {
        "forward the error back to the connection" in pending
      }
    }

    "it receives a ClearFor message for a userId" which {
      "is allowed to Clear the room" should {
        "send a Cleared message to the room" in pending
      }

      "is not allowed to Clear the room" should {
        "forward the error back to the connection" in pending
      }
    }

    "the room's owner terminates" should {
      val probe = TestProbe()
      val ownerRef = TestProbe().ref
      val memberProbe = TestProbe()
      val rpa = init(ownerRef)
      probe.watch(rpa)

      rpa ! JoinRoom(UserProxy(owner.id, memberProbe.ref))
      memberProbe.expectMsgType[RoomProxyActor.Subscribe]
      memberProbe.send(rpa, RoomProxyActor.Subscribed(rpa))
      expectMsgType[RoomUpdated]

      system.stop(ownerRef)

      "publish a room closed message" in {
        expectMsg(Closed(roomId))
      }

      "self-terminate" in {
        probe.expectTerminated(rpa)
      }
    }
  }

  def init(ownerRef: ActorRef = TestProbe().ref) =
    system.actorOf(RoomProxyActor.props(room, UserProxy(owner.id, ownerRef)))
}
