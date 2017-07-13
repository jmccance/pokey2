package pokey

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import pokey.application.ApplicationController
import pokey.connection.controller.ConnectionController

class AppRouter(
    appController: ApplicationController,
    connectionController: ConnectionController
) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/connect") => connectionController.connect
    case GET(p"/") => appController.index
    case GET(p"/$file*") => appController.assets("/public", file)
  }
}
