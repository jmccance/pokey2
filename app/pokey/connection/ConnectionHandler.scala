package pokey.connection

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pokey.connection.Requests._
import pokey.connection.Responses._
import pokey.room.RoomService
import pokey.user.UserService

class ConnectionHandler(userId: String,
                        client: ActorRef,
                        userService: UserService,
                        roomService: RoomService) extends Actor with ActorLogging {

  userService.subscribe(userId, self)

  override def receive: Receive = {
    case req: Request =>
      req match {
        case SetName(name) =>
          log.info("userId: {}, request: setName, name: {}", userId, name)
          userService.setName(userId, name)

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
          client ! ErrorResponse("Invalid request")
      }

    case pokey.user.events.UserUpdated(user) => client ! UserUpdated(user)
  }
}

object ConnectionHandler {
  val propsIdentifier = 'connectionHandlerProps

  def props(userId: String,
            client: ActorRef,
            userService: UserService,
            roomService: RoomService) = Props {
    new ConnectionHandler(
      userId,
      client,
      userService,
      roomService)
  }
}
