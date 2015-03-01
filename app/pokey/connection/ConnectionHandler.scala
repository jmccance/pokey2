package pokey.connection

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe
import pokey.room
import pokey.room.{RoomProxy, RoomService}
import pokey.user.UserProxy

class ConnectionHandler(userId: String,
                        userProxy: ActorRef,
                        roomService: RoomService,
                        client: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  import pokey.connection.ConnectionHandler._

  private[this] var rooms: Map[String, ActorRef] = Map.empty

  userProxy ! UserProxy.NewConnection(self)

  def receive: Receive = {
    case req: Request =>
      import pokey.connection.Requests._

      req match {
        case SetName(name) =>
          log.info("userId: {}, request: setName, name: {}", userId, name)
          userProxy ! UserProxy.SetName(name)

        case CreateRoom =>
          log.info("userId: {}, request: createRoom", userId)
          roomService.createRoomProxy(userId).map {
            case roomId: String => Events.RoomCreated(roomId)
          }.pipeTo(client)

        case JoinRoom(roomId) =>
          log.info("userId: {}, request: joinRoom, roomId: {}", userId, roomId)
          roomService.getRoomProxy(roomId).map {
            case Some(roomProxy: ActorRef) =>
              roomProxy ! RoomProxy.JoinRoom(userId)
              self ! RoomJoined(roomId, roomProxy)

            case None => Events.ErrorEvent(s"No room found with id '$roomId'")
          }

        case Estimate(roomId, value, comment) =>
          log.info("userId: {}, request: estimate, roomId: {}, value: {}, comment: {}",
            userId, roomId, value, comment)
          rooms(roomId) ! RoomProxy.SubmitEstimate(userId, room.Estimate(value, comment))

        case Reveal(roomId) =>
          log.info("userId: {}, request: reveal, roomId: {}", userId, roomId)
          rooms(roomId) ! RoomProxy.Reveal(userId)

        case Clear(roomId) =>
          log.info("userId: {}, request: clear, roomId: {}", userId, roomId)
          rooms(roomId) ! RoomProxy.Clear(userId)

        case InvalidRequest(json) =>
          log.error("userId: {}, request: invalidRequest: {}", userId, json.toString())
          client ! Events.ErrorEvent("Invalid request")
      }

    case UserProxy.UserUpdated(user) => client ! Events.UserUpdated(user)

    case RoomJoined(id, proxy) => rooms += (id -> proxy)
  }

  /**
   * Called when the WebSocket connection terminates. When this happens, notify all the rooms we
   * have cached that we are leaving them.
   */
  override def postStop(): Unit = {
    rooms.foreach {
      case (_, proxy) => proxy ! RoomProxy.LeaveRoom(userId)
    }
  }
}

object ConnectionHandler {
  val propsIdentifier = 'connectionHandlerProps

  def props(userId: String,
            userProxy: ActorRef,
            roomService: RoomService,
            client: ActorRef) = Props {
    new ConnectionHandler(userId, userProxy, roomService, client)
  }

  private[connection] case class RoomJoined(id: String, proxy: ActorRef)
}
