package pokey.room

import akka.actor.{ActorRef, Props, Actor, ActorLogging}
import play.api.libs.json.{Json, JsValue}

class RoomWSHandler(out: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case msg: JsValue => out ! Json.obj("message" -> msg)
  }
}

object RoomWSHandler {
  def props(out: ActorRef) = Props(new RoomWSHandler(out))
}
