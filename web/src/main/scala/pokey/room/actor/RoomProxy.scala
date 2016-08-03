package pokey.room.actor

import akka.actor.ActorRef

case class RoomProxy(id: String, ref: ActorRef)
