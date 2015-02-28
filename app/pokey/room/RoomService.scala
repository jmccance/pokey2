package pokey.room

import akka.actor.ActorRef
import pokey.util.Subscribable

trait RoomService extends Subscribable[String]

class DefaultRoomService extends RoomService {
  override def subscribe(id: String, subscriber: ActorRef): Unit = ???

  override def unsubscribe(id: String, subscriber: ActorRef): Unit = ???
}
