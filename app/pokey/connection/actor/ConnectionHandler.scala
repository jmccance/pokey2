package pokey.connection.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe
import pokey.connection.model.{Events, InvalidRequest, Request}
import pokey.room.actor.RoomProxyActor
import pokey.room.model.Estimate
import pokey.room.service.RoomService
import pokey.user.actor.{UserProxy, UserProxyActor}

class ConnectionHandler(userProxy: UserProxy,
                        roomService: RoomService,
                        client: ActorRef) extends Actor with ActorLogging {
  import ConnectionHandler._
  import context.dispatcher

  private[this] val userId = userProxy.id
  private[this] var rooms: Map[String, ActorRef] = Map.empty

  userProxy.actor ! UserProxyActor.NewConnection(self)

  def receive: Receive = {
    case req: Request =>
      import pokey.connection.model.Requests._

      req match {
        case SetNameRequest(name) =>
          log.info("userId: {}, request: setName, name: {}", userId, name)
          userProxy.actor ! UserProxyActor.SetName(name)

        case CreateRoomRequest =>
          log.info("userId: {}, request: createRoom", userId)
          roomService
            .createRoom(userId)
            .map(proxy => Events.RoomCreated(proxy.id))
            .pipeTo(client)

        case JoinRoomRequest(roomId) =>
          log.info("userId: {}, request: joinRoom, roomId: {}", userId, roomId)
          roomService.getRoom(roomId).map {
            case Some(roomProxy) =>
              roomProxy.actor ! RoomProxyActor.JoinRoom(userProxy)
              self ! RoomJoined(roomId, roomProxy.actor)

            case None => Events.ErrorEvent(s"No room found with id '$roomId'")
          }

        case SubmitEstimateRequest(roomId, value, comment) =>
          log.info("userId: {}, request: estimate, roomId: {}, value: {}, comment: {}",
            userId, roomId, value, comment)
          rooms(roomId) ! RoomProxyActor.SubmitEstimate(userId, Estimate(value, comment))

        case RevealRoomRequest(roomId) =>
          log.info("userId: {}, request: reveal, roomId: {}", userId, roomId)
          rooms(roomId) ! RoomProxyActor.Reveal(userId)

        case ClearRoomRequest(roomId) =>
          log.info("userId: {}, request: clear, roomId: {}", userId, roomId)
          rooms(roomId) ! RoomProxyActor.Clear(userId)

        case InvalidRequest(json) =>
          log.error("userId: {}, request: invalidRequest: {}", userId, json.toString())
          client ! Events.ErrorEvent("Invalid request")
      }

    case UserProxyActor.UserUpdated(user) => client ! Events.UserUpdated(user)

    case RoomJoined(id, proxy) => rooms += (id -> proxy)
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

  private[connection] case class RoomJoined(id: String, proxy: ActorRef)
}
