package pokey.room.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import pokey.room.actor.{ RoomProxy, RoomRegistry }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait RoomService {
  /**
   * @param ownerId the user id that should own this room
   * @return a Future with the id of a newly created room owned by the specified user id
   */
  def createRoom(ownerId: String)(implicit ec: ExecutionContext): Future[RoomProxy]

  /**
   * @param id the id of the room proxy to retrieve
   * @return a Future containing the proxy for this room, if it exists
   */
  def getRoom(id: String)(implicit ec: ExecutionContext): Future[Option[RoomProxy]]
}

class DefaultRoomService(roomRegistry: ActorRef) extends RoomService {
  private[this] implicit val timeout = Timeout(2.seconds)

  override def createRoom(ownerId: String)(implicit ec: ExecutionContext): Future[RoomProxy] =
    (roomRegistry ? RoomRegistry.CreateRoomFor(ownerId)).mapTo[RoomProxy]

  override def getRoom(id: String)(implicit ec: ExecutionContext): Future[Option[RoomProxy]] =
    (roomRegistry ? RoomRegistry.GetRoomProxy(id)).mapTo[Option[RoomProxy]]
}
