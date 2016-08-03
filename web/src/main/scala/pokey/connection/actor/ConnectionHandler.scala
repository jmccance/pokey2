package pokey.connection.actor

import akka.actor._
import play.api.mvc.WebSocket
import pokey.connection.actor.CommandHandlers._
import pokey.connection.model._
import pokey.room.actor.{RoomProxy, RoomProxyActor}
import pokey.room.service.RoomService
import pokey.user.actor.{UserProxy, UserProxyActor}

import scala.concurrent.duration._

class ConnectionHandler(
  roomService: RoomService,
  settings: ConnectionHandler.Settings,
  userProxy: UserProxy,
  client: ActorRef
) extends Actor
    with ActorLogging
    with CommandLoggers {
  import ConnectionHandler._
  import context.dispatcher
  import settings._

  private[this] val connUserId = userProxy.id
  private[this] var rooms: Map[String, ActorRef] = Map.empty
  private[this] val heartbeatCancellable =
    context.system.scheduler.schedule(
      heartbeatInterval,
      heartbeatInterval,
      client,
      Events.HeartbeatEvent
    )

  userProxy.ref ! UserProxyActor.NewConnection(self)
  client ! Events.ConnectionInfo(connUserId)

  def receive: Receive = {
    case req: Command =>
      (
        logCommands(connUserId)
        andThen handleCommandWith(client, connUserId, rooms, roomService, userProxy)
      )(req)

    case RoomJoined(roomProxy) =>
      rooms += roomProxy.id -> roomProxy.ref

    case message =>
      type EventMapping = PartialFunction[Any, Event]

      val userEvents: EventMapping = {
        import UserProxyActor._

        {
          case UserUpdated(user) => Events.UserUpdatedEvent(user)
        }
      }

      val roomEvents: EventMapping = {
        import RoomProxyActor._

        {
          case RoomUpdated(roomInfo) => Events.RoomUpdatedEvent(roomInfo)

          case UserJoined(roomId, user) => Events.UserJoinedEvent(roomId, user)

          case UserLeft(roomId, user) => Events.UserLeftEvent(roomId, user)

          case EstimateUpdated(roomId, userId, estimate) =>
            Events.EstimateUpdatedEvent(roomId, userId, estimate)

          case Revealed(roomId, estimates) => Events.RoomRevealedEvent(roomId, estimates)

          case Cleared(roomId) => Events.RoomClearedEvent(roomId)

          case Closed(roomId) =>
            // The room is closed, so we no longer need to hold onto the proxy
            rooms -= roomId
            Events.RoomClosedEvent(roomId)
        }
      }

      val event = (userEvents orElse roomEvents).lift(message)

      event.foreach(client ! _)
  }

  /**
   * Called when the WebSocket connection terminates. When this happens, notify all the rooms we
   * have cached that we are leaving them.
   */
  override def postStop(): Unit = {
    heartbeatCancellable.cancel()

    rooms.foreach {
      case (_, proxy) => proxy ! RoomProxyActor.LeaveRoom(userProxy)
    }
  }
}

object ConnectionHandler {
  case class Settings(heartbeatInterval: FiniteDuration)

  def props(
    roomService: RoomService,
    config: Settings
  )(userProxy: UserProxy)(client: ActorRef) =
    Props(new ConnectionHandler(roomService, config, userProxy, client))

  type PropsFactory = ((UserProxy) => WebSocket.HandlerProps)

  def propsFactory(roomService: RoomService, config: Settings): PropsFactory =
    props(roomService, config)

  private[actor] case class RoomJoined(roomProxy: RoomProxy)
}
