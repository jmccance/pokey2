package pokey.connection.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe
import pokey.connection.model.Events.ErrorEvent
import pokey.connection.model._
import pokey.room.actor.{RoomProxy, RoomProxyActor}
import pokey.room.model.Estimate
import pokey.room.service.RoomService
import pokey.user.actor.{UserProxy, UserProxyActor}

class ConnectionHandler(userProxy: UserProxy,
                        roomService: RoomService,
                        client: ActorRef) extends Actor with ActorLogging {
  import ConnectionHandler._
  import context.dispatcher

  private[this] val connUserId = userProxy.id
  private[this] var rooms: Map[String, ActorRef] = Map.empty

  userProxy.actor ! UserProxyActor.NewConnection(self)

  def receive: Receive = {
    case req: Command =>
      import pokey.connection.model.Commands._

      req match {
        case SetNameCommand(name) =>
          log.info("userId: {}, command: setName, name: {}", connUserId, name)
          userProxy.actor ! UserProxyActor.SetName(name)

        case CreateRoomCommand =>
          log.info("userId: {}, command: createRoom", connUserId)
          roomService
            .createRoom(connUserId)
            .map(proxy => Events.RoomCreated(proxy.id))
            .recover(ErrorEvent.mapThrowable)
            .pipeTo(client)

        case JoinRoomCommand(roomId) =>
          log.info("userId: {}, command: joinRoom, roomId: {}", connUserId, roomId)
          roomService.getRoom(roomId).map {
            case Some(roomProxy) =>
              roomProxy.actor ! RoomProxyActor.JoinRoom(userProxy)
              self ! RoomJoined(roomProxy)

            case None => Events.ErrorEvent(s"No room found with id '$roomId'")
          }

        // TODO Make below methods handle invalid commands more correctly.

        case SubmitEstimateCommand(roomId, value, comment) =>
          log.info("userId: {}, command: estimate, roomId: {}, value: {}, comment: {}",
            connUserId, roomId, value, comment)

          rooms.get(roomId) match {
            case Some(roomProxy) =>
              roomProxy ! RoomProxyActor.SubmitEstimate(connUserId, Estimate(value, comment))

            case None =>
              client ! ErrorEvent("Cannot reveal room that you have not joined.")
          }

        case RevealRoomCommand(roomId) =>
          log.info("userId: {}, command: reveal, roomId: {}", connUserId, roomId)
          rooms.get(roomId) match {
            case Some(roomProxy) => roomProxy ! RoomProxyActor.Reveal(connUserId)

            case None => client ! ErrorEvent("Cannot reveal room that you have not joined.")
          }

        case ClearRoomCommand(roomId) =>
          log.info("userId: {}, command: clear, roomId: {}", connUserId, roomId)
          rooms.get(roomId) match {
            case Some(roomProxy) => roomProxy ! RoomProxyActor.Clear(connUserId)

            case None => client ! ErrorEvent("Cannot clear room that you have not joined.")
          }

        case InvalidCommand(json) =>
          log.error("userId: {}, command: invalidCommand: {}", connUserId, json.toString())
          client ! Events.ErrorEvent("Invalid command")
      }

    case RoomJoined(roomProxy) =>
      rooms += roomProxy.id -> roomProxy.actor

    case message =>
      type EventMapping = PartialFunction[Any, Event]

      val userEvents: EventMapping = {
        import UserProxyActor._

        {
          case UserUpdated(user) => Events.UserUpdated(user)
        }
      }

      val roomEvents: EventMapping = {
        import RoomProxyActor._

        {
          case RoomUpdated(roomInfo) => Events.RoomUpdated(roomInfo)

          case UserJoined(roomId, user) => Events.UserJoined(roomId, user)

          case UserLeft(roomId, user) => Events.UserLeft(roomId, user)

          case EstimateUpdated(roomId, userId, estimate) =>
            Events.EstimateUpdated(roomId, userId, estimate)

          case Revealed(roomId, estimates) => Events.RoomRevealed(roomId, estimates)

          case Cleared(roomId) => Events.RoomCleared(roomId)

          case Closed(roomId) =>
            // The room is closed, so we no longer need to hold onto the proxy
            rooms -= roomId
            Events.RoomClosed(roomId)
        }
      }

      val event = (userEvents orElse roomEvents).lift(message)

      event.map(client ! _)
  }

  /**
   * Called when the WebSocket connection terminates. When this happens, notify all the rooms we
   * have cached that we are leaving them.
   */
  override def postStop(): Unit = {
    rooms.foreach {
      case (_, proxy) => proxy ! RoomProxyActor.LeaveRoom(userProxy)
    }
  }
}

object ConnectionHandler {
  val propsIdentifier = 'connectionHandlerProps

  def props(userProxy: UserProxy,
            roomService: RoomService,
            client: ActorRef) = Props {
    new ConnectionHandler(userProxy, roomService, client)
  }

  private case class RoomJoined(roomProxy: RoomProxy)
}
