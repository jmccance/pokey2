package pokey.room

import akka.actor.ActorRef

case class RoomProxy(id: String, actor: ActorRef)
