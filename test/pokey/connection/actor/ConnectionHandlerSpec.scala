package pokey.connection.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import pokey.connection.model.Events._
import pokey.room.actor.{RoomProxy, RoomProxyActor}
import pokey.room.model.{Estimate, RoomInfo}
import pokey.room.service.{RoomService, StubRoomService}
import pokey.test.AkkaUnitSpec
import pokey.user.actor.{UserProxy, UserProxyActor}
import pokey.user.model.User
import pokey.util.using

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ConnectionHandlerSpec extends AkkaUnitSpec {

  // In these tests, the role of the WebSocket client ("out") will be played by "self".

  val settings = ConnectionHandler.Settings(1.hour)
  val someTopic = "Hours to brew potion"

  "A ConnectionHandler" which {
    val userId = User.Id.unsafeFrom("1")
    val roomId = "42"
    val someUser = User.unsafeFrom("616", "Esme")
    val someEstimate = Estimate(Some("999"), Some("No can do"))

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
        val roomInfo = RoomInfo(roomId, User.Id.unsafeFrom("Bad Axe"), someTopic, isRevealed = false)

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

    def init(
      roomService: RoomService = new TestRoomService,
      userRef: ActorRef = TestProbe().ref
    ) = {
      using(
        system.actorOf(
          ConnectionHandler.props(roomService, settings)(UserProxy(userId, userRef))(self)
        )
      )(_ => expectMsgType[ConnectionInfo])
    }

    class TestRoomService(_rooms: (String, ActorRef)*) extends StubRoomService {
      private[this] val rooms = Map(_rooms: _*)

      // For the purposes of the current specs, this does not actually need to work.
      override def createRoom(ownerId: User.Id)(implicit ec: ExecutionContext): Future[RoomProxy] =
        Future.successful(RoomProxy(roomId, TestProbe().ref))

      override def getRoom(id: String)(implicit ec: ExecutionContext): Future[Option[RoomProxy]] =
        Future.successful(rooms.get(id).map(ref => RoomProxy(id, ref)))
    }
  }
}
