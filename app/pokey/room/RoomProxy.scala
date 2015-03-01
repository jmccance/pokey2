package pokey.room

import akka.actor._
import pokey.util.{Subscribable, TopicProtocol}

class RoomProxy(room: Room, ownerProxy: ActorRef)
  extends Actor
  with ActorLogging
  with Subscribable {

  override protected val protocol: TopicProtocol = RoomProxy

  override def preStart(): Unit = context.watch(ownerProxy)

  override def receive: Receive = handleSubscriptions orElse {
    case Terminated(`ownerProxy`) =>
      log.info("room_closed: {}, owner_id: {}", room.id, room.ownerId)
      // TODO Inform connections that the room has closed.
      context.stop(self)
  }
}

object RoomProxy extends TopicProtocol {
  def props(room: Room, ownerProxy: ActorRef) = Props(new RoomProxy(room, ownerProxy))
}
