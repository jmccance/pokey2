package pokey.connection

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pokey.connection.Requests._
import pokey.connection.Responses._

class ConnectionHandler(sessionId: String, client: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case SetName(name) =>
      log.info("sessionId={}, setName, name={}", sessionId, name)

    case CreateRoom =>
      log.info("sessionId={}, createRoom", sessionId)

    case JoinRoom(roomId) =>
      log.info("sessionId={}, joinRoom, roomId={}", sessionId, roomId)

    case Estimate(roomId, value, comment) =>
      log.info("sessionId={}, estimate, roomId={}, value={}, comment={}",
        sessionId, roomId, value, comment)

    case Reveal(roomId) =>
      log.info("sessionId={}, reveal, roomId={}", sessionId, roomId)

    case Clear(roomId) =>
      log.info("sessionId={}, clear, roomId={}", sessionId, roomId)

    case InvalidRequest(json) =>
      log.error("sessionId={}, invalidRequest={}", sessionId, json.toString())
      client ! ErrorResponse("Invalid request")
  }
}

object ConnectionHandler {
  def props(sessionId: String, client: ActorRef) = Props(new ConnectionHandler(sessionId, client))
}
