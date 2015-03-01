package pokey.application.module

import _root_.akka.actor.ActorRef
import pokey.assets.AssetController
import pokey.connection.{ConnectionController, ConnectionHandler}
import pokey.user.UserService
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
    (userId: String, userProxy: ActorRef) =>
      ConnectionHandler.props(
        userId,
        userProxy,
        _: ActorRef)
  })
}
