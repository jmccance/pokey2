package pokey.assets

import controllers.Assets
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import pokey.user.UserService
import scaldi.Injectable._
import scaldi.Injector

class AssetController(implicit inj: Injector) extends Controller {

  val userService = inject [UserService]

  def assets(path: String, file: String) = Action.async { request =>
    val updatedSession = request.session.get("user_id") match {
      case Some(sessionId) => request.session

      case None => addUserIdTo(request.session)
    }

    Assets.at(path, file)(request).map(_.withSession(updatedSession))
  }

  private[this] def addUserIdTo(session: Session) =
    session + ("user_id" -> userService.nextUserId())
}
