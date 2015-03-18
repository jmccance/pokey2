package pokey.application.module

import pokey.assets.controller.AssetController
import pokey.connection.actor.ConnectionHandler
import pokey.connection.controller.ConnectionController
import pokey.room.service.RoomService
import pokey.user.service.UserService
import scaldi._

class WebModule extends Module {
  binding to injected [AssetController]

  binding to injected [ConnectionController] (
    'userService -> inject [UserService],
    'connectionHandlerProps -> inject [ConnectionHandler.PropsFactory]
  )

  (bind [ConnectionHandler.PropsFactory]
    to {
    val roomService = inject [RoomService]

    ConnectionHandler.propsFactory(roomService)
  })
}
