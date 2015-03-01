package pokey.room

import akka.actor._
import pokey.user.UserService

class RoomRegistry(userService: UserService) extends Actor with ActorLogging {
  import context.dispatcher
  import pokey.room.RoomRegistry._
  
  def receive = withRooms(Map.empty)

  private[this] def withRooms(rooms: Map[String, ActorRef]): Receive = {
    case GetRoomProxy(id) if rooms.contains(id) => sender ! rooms(id)

    case CreateRoomFor(ownerId) =>
      val querent = sender()
      userService.getUserProxy(ownerId).map {
        case Some(userProxy) => self ! CreateRoomProxy(querent, ownerId, userProxy)

        case None =>
          querent ! CreateRoomError(s"Could not create room for nonexistent user $ownerId")
      }

    case CreateRoomProxy(querent, ownerId, ownerProxy) =>
      val room = Room(nextId(), ownerId)
      val roomProxy = context.actorOf(RoomProxy.props(room, ownerProxy))
      context.watch(roomProxy)
      become(rooms + (room.id -> roomProxy))
      log.info("new_room: {}", room)
      querent ! roomProxy

    case Terminated(roomProxy) =>
      val deadRoom = rooms.find {
        case (_, proxy) => proxy == roomProxy
      }

      deadRoom.map {
        case (id, proxy) =>
          log.info("room_closed: {}", id)
          become(rooms - id)
      }.orElse {
        log.warning("unknown_room_closed: {}", roomProxy)
        None
      }
  }

  private[this] def nextId(): String = new java.rmi.server.UID().toString

  private[this] def become(rooms: Map[String, ActorRef]) = context.become(withRooms(rooms))
}

object RoomRegistry {
  val identifier = 'roomRegistry

  def props(userService: UserService) = Props(new RoomRegistry(userService))

  case class GetRoomProxy(id: String)

  case class CreateRoomFor(ownerId: String)

  /**
   * Internal message used to handle creating the room once we've asynchronously received the
   * owner's proxy.
   *
   * @param querent the original actor that asked for the room to be created
   * @param ownerId the id of the user who should own the room
   * @param ownerProxy the user proxy corresponding to the owner id
   */
  private[room] case class CreateRoomProxy(querent: ActorRef, ownerId: String, ownerProxy: ActorRef)

  case class CreateRoomError(message: String)
}
