package pokey.room.service

import pokey.room.actor.RoomProxy

import concurrent.{ ExecutionContext, Future }

class StubRoomService extends RoomService {
  override def createRoom(ownerId: String)(implicit ec: ExecutionContext): Future[RoomProxy] = ???

  override def getRoom(id: String)(implicit ec: ExecutionContext): Future[Option[RoomProxy]] = ???
}
