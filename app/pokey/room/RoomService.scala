package pokey.room

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait RoomService {
  /**
   * @param ownerId the user id that should own this room
   * @return a Future with the id of a newly created room owned by the specified user id
   */
  def createRoomProxy(ownerId: String)(implicit ec: ExecutionContext): Future[(String, ActorRef)]

  /**
   * @param id the id of the room proxy to retrieve
   * @return a Future containing the proxy for this room, if it exists
   */
  def getRoomProxy(id: String)(implicit ec: ExecutionContext): Future[Option[ActorRef]]
}

class DefaultRoomService(roomRegistry: ActorRef) extends RoomService {
  private[this] implicit val timeout = Timeout(2.seconds)

  override def createRoomProxy(ownerId: String)
                              (implicit ec: ExecutionContext): Future[(String, ActorRef)] =
    (roomRegistry ? RoomRegistry.CreateRoomFor(ownerId)).mapTo[(String, ActorRef)]

  override def getRoomProxy(id: String)(implicit ec: ExecutionContext): Future[Option[ActorRef]] =
    (roomRegistry ? RoomRegistry.GetRoomProxy(id)).mapTo[Option[ActorRef]]
}
