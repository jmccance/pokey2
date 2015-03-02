package pokey.application.module

import _root_.akka.actor.ActorRef
import pokey.assets.AssetController
import pokey.connection.{ConnectionController, ConnectionHandler}
import pokey.room.RoomService
import pokey.user.{UserProxy, UserService}
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
    toProvider {
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
