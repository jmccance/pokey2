package pokey.application

import com.typesafe.config.{Config, ConfigException}
import controllers.Assets
import org.scalactic._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import pokey.user.service.UserService

class ApplicationController(
    settings: ApplicationController.Settings,
    userService: UserService
) extends Controller {
  import settings._

  def assets(path: String, file: String) = Action.async { request =>
    val updatedSession = request.session.get("user_id") match {
      case Some(sessionId) => request.session

      case None => request.session + ("user_id" -> userService.nextUserId())
    }

    Assets.at(path, file)(request).map(_.withSession(updatedSession))
  }

  def index = Action(Ok(views.html.index(oTrackingId)))
}

object ApplicationController {
  case class Settings(oTrackingId: Option[String])

  object Settings {
    def from(config: Config): Settings Or Throwable = {
      attempt(config.getString("tracking-id"))
        .map(Option(_))
        .recoverWith {
          case _: ConfigException.Missing => Good(None)
          case t => Bad(t)
        }
        .map(Settings.apply)
    }
  }
}
