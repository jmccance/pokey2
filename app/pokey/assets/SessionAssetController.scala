package pokey.assets

import controllers.Assets
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import pokey.session.SessionService
import scaldi.{Injectable, Injector}

import scala.concurrent.Future

class SessionAssetController(implicit inj: Injector) extends Controller with Injectable {

  val sessionService = inject [SessionService]

  def assets(path: String, file: String) = Action.async { request =>
    val fSession = request.session.get("session_id") match {
      case Some(sessionId) =>
        sessionService.isValid(sessionId).flatMap {
          case true => Future.successful(request.session)
          case false =>
            newPokeySession(request.session)
        }

      case None => newPokeySession(request.session)
    }

    fSession.flatMap { session =>
      Assets.at(path, file)(request).map(_.withSession(session))
    }
  }

  private[this] def newPokeySession(session: Session) =
    sessionService.newSession().map { sessionId =>
      session + ("session_id" -> sessionId)
    }
}
