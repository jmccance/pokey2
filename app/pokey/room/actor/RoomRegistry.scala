package pokey.room.actor

import akka.actor._
import pokey.room.model.Room
import pokey.user.actor.UserProxy
import pokey.user.service.UserService

class RoomRegistry(userService: UserService) extends Actor with ActorLogging {
  import RoomRegistry._
  import context.dispatcher
  
  def receive = withRooms(Map.empty)

  private[this] def withRooms(rooms: Map[String, RoomProxy]): Receive = {
    case GetRoomProxy(id) => sender ! rooms.get(id)

    case CreateRoomFor(ownerId) =>
      val querent = sender()
      userService.getUser(ownerId).map {
        case Some(userProxy) => self ! CreateRoomProxy(querent, userProxy)

        case None =>
          querent ! CreateRoomError(s"Could not create room for nonexistent user $ownerId")
      }

    case CreateRoomProxy(querent, ownerProxy) =>
      val room = Room(nextId(), ownerProxy.id)
      val roomProxy = RoomProxy(
        room.id,
        context.actorOf(RoomProxyActor.props(room, ownerProxy), s"room-proxy-${room.id}"))
      context.watch(roomProxy.actor)
      become(rooms + (room.id -> roomProxy))
      log.info("new_room: {}", room)
      querent ! roomProxy

    case Terminated(deadActor) =>
      val deadRoom = rooms.find {
        case (_, proxy) => proxy.actor == deadActor
      }

      deadRoom.map {
        case (id, proxy) =>
          log.info("room_closed: {}", proxy)
          become(rooms - id)
      }.orElse {
        log.warning("unknown_room_closed: {}", deadActor)
        None
      }
  }

  private[this] def nextId(): String = new java.rmi.server.UID().toString

  private[this] def become(rooms: Map[String, RoomProxy]) = context.become(withRooms(rooms))
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
   * @param ownerProxy the user proxy corresponding to the owner id
   */
  private case class CreateRoomProxy(querent: ActorRef, ownerProxy: UserProxy)

  case class CreateRoomError(message: String)
}
