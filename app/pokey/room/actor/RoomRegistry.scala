package pokey.room.actor

import akka.actor._
import pokey.room.model.Room
import pokey.user.actor.UserProxy
import pokey.user.service.UserService

class RoomRegistry(private[this] var ids: Stream[String], userService: UserService) extends Actor with ActorLogging {
  import RoomRegistry._
  import context.dispatcher
  
  def receive = withRooms(Map.empty)

  private[this] def withRooms(rooms: Map[String, RoomProxy]): Receive = {
    case GetRoomProxy(id) => sender ! rooms.get(id)

    case CreateRoomFor(ownerId) =>
      val querent = sender()
      userService.getUser(ownerId).foreach {
        case Some(userProxy) => self ! CreateRoomProxy(querent, userProxy)

        case None =>
          querent ! CreateRoomError(s"Could not create room for nonexistent user $ownerId")
      }

    case CreateRoomProxy(querent, ownerProxy) =>
      val id #:: rest = ids
      ids = rest

      val room = Room(id, ownerProxy.id)
      val roomProxy = RoomProxy(
        room.id,
        context.actorOf(RoomProxyActor.props(room, ownerProxy), s"room-proxy-${room.id}"))
      context.watch(roomProxy.ref)
      become(rooms + (room.id -> roomProxy))
      log.info("new_room: {}", room)
      querent ! roomProxy

    case Terminated(deadActor) =>
      val deadRoom = rooms.find {
        case (_, proxy) => proxy.ref == deadActor
      }

      deadRoom.foreach {
        case (id, proxy) =>
          log.info("room_closed: {}", proxy)
          become(rooms - id)
      }
  }

  private[this] def become(rooms: Map[String, RoomProxy]) = context.become(withRooms(rooms))
}

object RoomRegistry {
  val identifier = 'roomRegistry

  def props(ids: Stream[String],
            userService: UserService) = Props(new RoomRegistry(ids, userService))

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
