package pokey.room

import akka.actor.{Actor, ActorLogging, Props}
import pokey.util.{Subscribable, TopicProtocol}

class RoomProxy(room: Room) extends Actor with ActorLogging with Subscribable {
  override protected val protocol: TopicProtocol = RoomProxy

  override def receive: Receive = handleSubscriptions
}

object RoomProxy extends TopicProtocol {
  def props(room: Room) = Props(new RoomProxy(room))
}
