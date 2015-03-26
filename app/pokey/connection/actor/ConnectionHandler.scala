package pokey.connection.actor

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.pipe
import play.api.mvc.WebSocket
import pokey.connection.model.Events.ErrorEvent
import pokey.connection.model._
import pokey.room.actor.{ RoomProxy, RoomProxyActor }
import pokey.room.service.RoomService
import pokey.user.actor.{ UserProxy, UserProxyActor }

class ConnectionHandler(roomService: RoomService,
                        userProxy: UserProxy,
                        client: ActorRef) extends Actor with ActorLogging {
  import ConnectionHandler._
  import context.dispatcher

  private[this] val connUserId = userProxy.id
  private[this] var rooms: Map[String, ActorRef] = Map.empty

  userProxy.ref ! UserProxyActor.NewConnection(self)
  client ! Events.ConnectionInfo(connUserId)

  def receive: Receive = {
    case req: Command =>
      import pokey.connection.model.Commands._

      req match {
        case SetNameCommand(name) =>
          log.info("userId: {}, command: setName, name: {}", connUserId, name)
          userProxy.ref ! UserProxyActor.SetName(name)

        case CreateRoomCommand =>
          log.info("userId: {}, command: createRoom", connUserId)
          roomService
            .createRoom(connUserId)
            .map(proxy => Events.RoomCreatedEvent(proxy.id))
            .recover(ErrorEvent.mapThrowable)
            .pipeTo(client)
          ()

        case JoinRoomCommand(roomId) =>
          log.info("userId: {}, command: joinRoom, roomId: {}", connUserId, roomId)
          roomService.getRoom(roomId).foreach {
            case Some(roomProxy) =>
              roomProxy.ref ! RoomProxyActor.JoinRoom(userProxy)
              self ! RoomJoined(roomProxy)

            case None => client ! Events.ErrorEvent(s"No room found with id '$roomId'")
          }

        // TODO Make below methods handle invalid commands more correctly.

        case SubmitEstimateCommand(roomId, estimate) =>
          log.info("userId: {}, command: estimate, roomId: {}, estimate: {}",
            connUserId, roomId, estimate)

          rooms.get(roomId) match {
            case Some(roomProxy) =>
              roomProxy ! RoomProxyActor.SubmitEstimate(connUserId, estimate)

            case None =>
              client ! ErrorEvent(s"Room $roomId is not associated with this connection")
          }

        case RevealRoomCommand(roomId) =>
          log.info("userId: {}, command: reveal, roomId: {}", connUserId, roomId)
          rooms.get(roomId) match {
            case Some(roomProxy) => roomProxy ! RoomProxyActor.RevealFor(connUserId)

            case None => client ! ErrorEvent(s"Room $roomId is not associated with this connection")
          }

        case ClearRoomCommand(roomId) =>
          log.info("userId: {}, command: clear, roomId: {}", connUserId, roomId)
          rooms.get(roomId) match {
            case Some(roomProxy) => roomProxy ! RoomProxyActor.ClearFor(connUserId)

            case None => client ! ErrorEvent(s"Room $roomId is not associated with this connection")
          }

        case InvalidCommand(json) =>
          log.error("userId: {}, command: invalidCommand: {}", connUserId, json.toString())
          client ! Events.ErrorEvent("Invalid command")
      }

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
    rooms.foreach {
      case (_, proxy) => proxy ! RoomProxyActor.LeaveRoom(userProxy)
    }
  }
}

object ConnectionHandler {
  def props(roomService: RoomService,
            userProxy: UserProxy,
            client: ActorRef) = Props(new ConnectionHandler(roomService, userProxy, client))

  type PropsFactory = ((UserProxy) => WebSocket.HandlerProps)

  def propsFactory(roomService: RoomService): PropsFactory = user => props(roomService, user, _)

  private case class RoomJoined(roomProxy: RoomProxy)
}
