package pokey.application.module

import _root_.akka.actor.ActorRef
import pokey.assets.controller.AssetController
import pokey.connection.actor.ConnectionHandler
import pokey.connection.controller.ConnectionController
import pokey.room.service.RoomService
import pokey.user.actor.UserProxy
import pokey.user.service.UserService
import scaldi._

class WebModule extends Module {
  binding to injected [AssetController]
  binding to injected [ConnectionController] (
    'userService -> inject [UserService],
    'connectionHandlerProps ->
      inject [ConnectionController.HandlerPropsFactory] (
        identified by ConnectionHandler.propsIdentifier
      )
  )

  (bind [ConnectionController.HandlerPropsFactory]
    identifiedBy required(ConnectionHandler.propsIdentifier)
    to {
    val roomService = inject [RoomService]

    (userProxy: UserProxy) =>
      // Take advantage of partial function application to convert the ConnectionHandler.props
      // method to the appropriate signature for a WebSocket.HandlerProps.
      ConnectionHandler.props(
        userProxy,
        roomService,
        _: ActorRef)
  })
}
