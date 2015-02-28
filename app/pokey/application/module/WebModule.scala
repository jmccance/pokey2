package pokey.application.module

import _root_.akka.actor.ActorRef
import _root_.play.api.mvc.WebSocket
import pokey.assets.AssetController
import pokey.connection.{ConnectionController, ConnectionHandler}
import pokey.room.RoomService
import pokey.user.UserService
import scaldi._

class WebModule extends Module {
  binding to injected [AssetController]
  binding to injected [ConnectionController] (
    'userService -> inject [UserService],
    'connectionHandlerProps ->
      inject [String => WebSocket.HandlerProps] (identified by ConnectionHandler.propsIdentifier)
  )

  (bind [String => WebSocket.HandlerProps]
    identifiedBy required(ConnectionHandler.propsIdentifier)
    toProvider {
    val userService = inject [UserService]
    val roomService = inject [RoomService]

    (userId: String) =>
      ConnectionHandler.props(
        userId, _: ActorRef,
        userService,
        roomService)
  })
}
