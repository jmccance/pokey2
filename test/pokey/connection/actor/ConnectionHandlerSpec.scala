package pokey.connection.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import play.api.libs.json.JsString
import pokey.connection.model.Commands._
import pokey.connection.model.Events._
import pokey.connection.model.InvalidCommand
import pokey.room.actor.{ RoomProxy, RoomProxyActor }
import pokey.room.model.{ Estimate, RoomInfo }
import pokey.room.service.{ RoomService, StubRoomService }
import pokey.test.AkkaUnitSpec
import pokey.user.actor.{ UserProxy, UserProxyActor }
import pokey.user.model.User
import pokey.util.using

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

class ConnectionHandlerSpec extends AkkaUnitSpec {

  // In these tests, the role of the WebSocket client ("out") will be played by "self".

  val settings = ConnectionHandler.Settings(1.hour)

  "A ConnectionHandler" which {
    val userId = "1"
    val roomId = "42"
    val someUser = User("616", "Esme")
    val someEstimate = Estimate(Some("999"), Some("No can do"))

    "receives a SetNameCommand" should {
      "send a SetName message to its UserProxy" in {
        val userProbe = TestProbe()
        val handler = initWithProbe(userProbe = userProbe)
        handler ! SetNameCommand("Magrat")

        userProbe.expectMsg(UserProxyActor.SetName("Magrat"))
      }
    }

    "receives a CreateRoomCommand" should {
      "create a room for its UserProxy" in {
        val roomService = new TestRoomService(roomId -> TestProbe().ref)
        val handler = init(roomService)
        handler ! CreateRoomCommand

        expectMsg(RoomCreatedEvent(roomId))
      }
    }

    "receives a JoinRoomCommand" when {
      "the room exists" should {
        "send a JoinRoom message to the RoomProxy" in {
          val roomProbe = TestProbe()
          val userRef = TestProbe().ref
          val roomService = new TestRoomService(roomId -> roomProbe.ref)
          val handler = init(roomService, userRef)

          handler ! JoinRoomCommand(roomId)
          roomProbe.expectMsg(RoomProxyActor.JoinRoom(UserProxy("1", userRef)))
        }
      }

      "the room does not exist" should {
        "reply with an ErrorEvent" in {
          val userRef = TestProbe().ref
          val handler = init(userRef = userRef)

          handler ! JoinRoomCommand(roomId)
          expectMsgType[ErrorEvent]
        }
      }
    }

    "receives a SubmitEstimateCommand" when {
      "the room is known" should {
        "send a SubmitEstimate message to the RoomProxy" in {
          val roomProbe = TestProbe()
          val roomService = new TestRoomService(roomId -> roomProbe.ref)
          val userRef = TestProbe().ref
          val handler = init(roomService, userRef)

          handler ! JoinRoomCommand(roomId)
          roomProbe.expectMsg(RoomProxyActor.JoinRoom(UserProxy("1", userRef)))
          handler ! SubmitEstimateCommand(roomId, someEstimate)
          roomProbe.expectMsg(RoomProxyActor.SubmitEstimate(userId, someEstimate))
        }
      }

      "the room is not known" should {
        "reply with an ErrorEvent" in {
          val handler = init()

          handler ! SubmitEstimateCommand(roomId, someEstimate)
          expectMsgType[ErrorEvent]
        }
      }
    }

    "receives a RevealRoomCommand" when {
      "the room is known" should {
        "send a RevealRoom message to the RoomProxy" in {
          val roomProbe = TestProbe()
          val roomService = new TestRoomService(roomId -> roomProbe.ref)
          val userRef = TestProbe().ref
          val handler = init(roomService, userRef)

          handler ! JoinRoomCommand(roomId)
          roomProbe.expectMsg(RoomProxyActor.JoinRoom(UserProxy("1", userRef)))
          handler ! RevealRoomCommand(roomId)
          roomProbe.expectMsg(RoomProxyActor.RevealFor(userId))
        }
      }

      "the room is not known" should {
        "reply with an ErrorEvent" in {
          val handler = init()

          handler ! RevealRoomCommand(roomId)
          expectMsgType[ErrorEvent]
        }
      }
    }

    "receives a ClearRoomCommand" when {
      "the room is known" should {
        "send a ClearRoom message to the RoomProxy" in {
          val roomProbe = TestProbe()
          val roomService = new TestRoomService(roomId -> roomProbe.ref)
          val userRef = TestProbe().ref
          val handler = init(roomService, userRef)

          handler ! JoinRoomCommand(roomId)
          roomProbe.expectMsg(RoomProxyActor.JoinRoom(UserProxy("1", userRef)))
          handler ! ClearRoomCommand(roomId)
          roomProbe.expectMsg(RoomProxyActor.ClearFor(userId))
        }
      }

      "the room is not known" should {
        "reply with an ErrorEvent" in {
          val handler = init()

          handler ! ClearRoomCommand(roomId)
          expectMsgType[ErrorEvent]
        }
      }
    }

    "receives an invalid command" should {
      "reply with an error event" in {
        val handler = init()

        handler ! InvalidCommand(JsString("wut"))
        expectMsgType[ErrorEvent]
      }
    }

    "receives a UserUpdated message" should {
      "send a UserUpdated event to the client" in {
        val handler = init()
        handler ! UserProxyActor.UserUpdated(someUser)
        expectMsg(UserUpdatedEvent(someUser))
      }
    }

    "receives a RoomUpdated message" should {
      "send a RoomUpdated event to the client" in {
        val handler = init()
        val roomInfo = RoomInfo(roomId, "Bad Axe", isRevealed = false)

        handler ! RoomProxyActor.RoomUpdated(roomInfo)
        expectMsg(RoomUpdatedEvent(roomInfo))
      }
    }

    "receives a UserJoined message" should {
      "send a UserJoinedEvent to the client" in {
        val handler = init()

        handler ! RoomProxyActor.UserJoined(roomId, someUser)
        expectMsg(UserJoinedEvent(roomId, someUser))
      }
    }

    "receives a UserLeft message" should {
      "send a UserLeftEvent to the client" in {
        val handler = init()

        handler ! RoomProxyActor.UserLeft(roomId, someUser)
        expectMsg(UserLeftEvent(roomId, someUser))
      }
    }

    "receives a EstimateUpdated message" should {
      "send a EstimateUpdatedEvent to the client" in {
        val handler = init()

        handler ! RoomProxyActor.EstimateUpdated(roomId, userId, Option(someEstimate.asRevealed))
        expectMsg(EstimateUpdatedEvent(roomId, userId, Option(someEstimate.asRevealed)))
      }
    }

    "receives a Revealed message" should {
      "send a RoomRevealedEvent to the client" in {
        val handler = init()
        val estimates = Map(userId -> Option(someEstimate.asRevealed))

        handler ! RoomProxyActor.Revealed(roomId, estimates)
        expectMsg(RoomRevealedEvent(roomId, estimates))
      }
    }

    "receives a Cleared message" should {
      "send a RoomClearedEvent to the client" in {
        val handler = init()

        handler ! RoomProxyActor.Cleared(roomId)
        expectMsg(RoomClearedEvent(roomId))
      }
    }

    "receives a Closed message" should {
      "send a RoomClosedEvent to the client" in {
        val handler = init()

        handler ! RoomProxyActor.Closed(roomId)
        expectMsg(RoomClosedEvent(roomId))
      }
    }

    def init(roomService: RoomService = new TestRoomService,
             userRef: ActorRef = TestProbe().ref) =
      using(system.actorOf(ConnectionHandler.props(roomService, settings)(UserProxy(userId, userRef))(self))) { handler =>
        expectMsgType[ConnectionInfo]
      }

    def initWithProbe(roomService: RoomService = new TestRoomService,
                      userProbe: TestProbe = TestProbe()) =
      using(system.actorOf(ConnectionHandler.props(roomService, settings)(UserProxy(userId, userProbe.ref))(self))) { handler =>
        userProbe.expectMsg(UserProxyActor.NewConnection(handler))
        expectMsgType[ConnectionInfo]
      }

    class TestRoomService(_rooms: (String, ActorRef)*) extends StubRoomService {
      private[this] val rooms = Map(_rooms: _*)

      // For the purposes of the current specs, this does not actually need to work.
      override def createRoom(ownerId: String)(implicit ec: ExecutionContext): Future[RoomProxy] =
        Future.successful(RoomProxy(roomId, TestProbe().ref))

      override def getRoom(id: String)(implicit ec: ExecutionContext): Future[Option[RoomProxy]] =
        Future.successful(rooms.get(id).map(ref => RoomProxy(id, ref)))
    }
  }
}
