package pokey.room.actor

import akka.actor.ActorRef
import pokey.room.model.Room

case class RoomProxy(id: Room.Id, ref: ActorRef)
