package pokey.connection

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pokey.connection.Events._
import pokey.connection.Requests._
import pokey.room.RoomService
import pokey.user.UserProxy

class ConnectionHandler(userId: String,
                        userProxy: ActorRef,
                        roomService: RoomService,
                        client: ActorRef) extends Actor with ActorLogging {

  userProxy ! UserProxy.NewConnection(self)

  override def receive: Receive = {
    case req: Request =>
      req match {
        case SetName(name) =>
          log.info("userId: {}, request: setName, name: {}", userId, name)
          userProxy ! UserProxy.SetName(name)

        case CreateRoom =>
          log.info("userId: {}, request: createRoom", userId)

        case JoinRoom(roomId) =>
          log.info("userId: {}, request: joinRoom, roomId: {}", userId, roomId)

        case Estimate(roomId, value, comment) =>
          log.info("userId: {}, request: estimate, roomId: {}, value: {}, comment: {}",
            userId, roomId, value, comment)

        case Reveal(roomId) =>
          log.info("userId: {}, request: reveal, roomId: {}", userId, roomId)

        case Clear(roomId) =>
          log.info("userId: {}, request: clear, roomId: {}", userId, roomId)

        case InvalidRequest(json) =>
          log.error("userId: {}, request: invalidRequest: {}", userId, json.toString())
          client ! ErrorEvent("Invalid request")
      }

    case UserProxy.UserUpdated(user) => client ! UserUpdated(user)
  }
}

object ConnectionHandler {
  val propsIdentifier = 'connectionHandlerProps

  def props(userId: String,
            userProxy: ActorRef,
            roomService: RoomService,
            client: ActorRef) = Props {
    new ConnectionHandler(userId, userProxy, roomService, client)
  }
}
