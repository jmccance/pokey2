package pokey.application.module

import play.api.Configuration
import pokey.application.ApplicationController
import pokey.connection.actor.ConnectionHandler
import pokey.connection.controller.ConnectionController
import pokey.room.service.RoomService
import pokey.user.service.UserService
import scaldi._

import scala.concurrent.duration._

class WebModule extends Module {
  bind[ApplicationController.Settings] to {
    val config = inject[Configuration].underlying

    ApplicationController.Settings.from(config.getConfig("pokey"))
      .fold(
        identity,
        errors => sys.error(errors.toString)
      )
  }

  binding to injected[ApplicationController]

  binding to injected[ConnectionController](
    'userService -> inject[UserService],
    'connectionHandlerProps -> inject[ConnectionHandler.PropsFactory]
  )

  bind[ConnectionHandler.Settings] to {
    val config = inject[Configuration]

    ConnectionHandler.Settings(
      config.getMilliseconds("pokey.connection.heartbeat-interval").get.millis
    )
  }

  (bind[ConnectionHandler.PropsFactory]
    to {
      val roomService = inject[RoomService]
      val config = inject[ConnectionHandler.Settings]

      ConnectionHandler.propsFactory(roomService, config)
    })
}
