package pokey.room.service

import pokey.room.actor.RoomProxy
import pokey.room.model.Room
import pokey.user.model.User

import scala.concurrent.{ExecutionContext, Future}

class StubRoomService extends RoomService {
  override def createRoom(ownerId: User.Id)(implicit ec: ExecutionContext): Future[RoomProxy] = ???

  override def getRoom(id: Room.Id)(implicit ec: ExecutionContext): Future[Option[RoomProxy]] = ???
}
