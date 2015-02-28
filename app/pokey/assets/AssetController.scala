package pokey.assets

import controllers.Assets
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import pokey.user.UserService

class AssetController(userService: UserService) extends Controller {

  def assets(path: String, file: String) = Action.async { request =>
    val updatedSession = request.session.get("user_id") match {
      case Some(sessionId) => request.session

      case None =>request.session + ("user_id" -> userService.nextUserId())
    }

    Assets.at(path, file)(request).map(_.withSession(updatedSession))
  }
}
