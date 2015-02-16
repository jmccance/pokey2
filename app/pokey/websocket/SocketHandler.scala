package pokey.websocket

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pokey.websocket.Requests._
import pokey.websocket.Responses._

class SocketHandler(sessionId: String, client: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case SetName(name) => log.info("session_id={}, name_change={}", sessionId, name)
    case JoinRoom(roomId) => log.info("session_id={}, room_id={}", sessionId, roomId)
    case InvalidRequest(json) => client ! ErrorResponse("Invalid request")
  }
}

object SocketHandler {
  def props(sessionId: String, client: ActorRef) = Props(new SocketHandler(sessionId, client))
}
