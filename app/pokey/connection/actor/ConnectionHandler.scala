package pokey.connection.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe
import pokey.connection.model.{Event, Events, InvalidRequest, Request}
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
    case req: Request =>
      import pokey.connection.model.Requests._

      req match {
        case SetNameRequest(name) =>
          log.info("userId: {}, request: setName, name: {}", connUserId, name)
          userProxy.actor ! UserProxyActor.SetName(name)

        case CreateRoomRequest =>
          log.info("userId: {}, request: createRoom", connUserId)
          roomService
            .createRoom(connUserId)
            .map(proxy => Events.RoomCreated(proxy.id))
            .pipeTo(client)

        case JoinRoomRequest(roomId) =>
          log.info("userId: {}, request: joinRoom, roomId: {}", connUserId, roomId)
          roomService.getRoom(roomId).map {
            case Some(roomProxy) =>
              roomProxy.actor ! RoomProxyActor.JoinRoom(userProxy)
              self ! RoomJoined(roomProxy)

            case None => Events.ErrorEvent(s"No room found with id '$roomId'")
          }

        // TODO Make below methods handle invalid requests more correctly.

        case SubmitEstimateRequest(roomId, value, comment) =>
          log.info("userId: {}, request: estimate, roomId: {}, value: {}, comment: {}",
            connUserId, roomId, value, comment)
          rooms(roomId) ! RoomProxyActor.SubmitEstimate(connUserId, Estimate(value, comment))

        case RevealRoomRequest(roomId) =>
          log.info("userId: {}, request: reveal, roomId: {}", connUserId, roomId)
          rooms(roomId) ! RoomProxyActor.Reveal(connUserId)

        case ClearRoomRequest(roomId) =>
          log.info("userId: {}, request: clear, roomId: {}", connUserId, roomId)
          rooms(roomId) ! RoomProxyActor.Clear(connUserId)

        case InvalidRequest(json) =>
          log.error("userId: {}, request: invalidRequest: {}", connUserId, json.toString())
          client ! Events.ErrorEvent("Invalid request")
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

  private[actor] case class RoomJoined(roomProxy: RoomProxy)
}
