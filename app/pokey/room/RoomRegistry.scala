package pokey.room

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
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

    case CreateRoomProxy(querent, ownerId, userProxy) =>
      val room = Room(nextId(), ownerId)
      context.watch(userProxy)
      val roomProxy = context.actorOf(RoomProxy.props(room))
      become(rooms + (room.id -> roomProxy))
      log.info("new_room: {}", room)
      querent ! roomProxy
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
   * @param userProxy the user proxy corresponding to the owner id
   */
  private[room] case class CreateRoomProxy(querent: ActorRef, ownerId: String, userProxy: ActorRef)

  case class CreateRoomError(message: String)
}
