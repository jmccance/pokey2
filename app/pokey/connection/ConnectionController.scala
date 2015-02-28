package pokey.connection

import akka.actor.ActorRef
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import pokey.user.UserService
import scaldi.Injectable._
import scaldi.Injector

import scala.concurrent.Future

class ConnectionController(implicit inj: Injector) extends Controller {
  private[this] val log = Logger(this.getClass)

  private[this] val userService = inject [UserService]

  def connect = WebSocket.tryAcceptWithActor[Request, Response] { request =>
    log.info("Received WebSocket connection request")
    val oUserId = request.session.get("user_id")
    oUserId match {
      case Some(sessionId) =>
        userService.getUserWithSocket(sessionId).map {
          case (user, publisher) =>
            Right(ConnectionHandler.props(sessionId, _: ActorRef))
        }

      case None => Future.successful(Left(Unauthorized("Missing user id")))
    }

  }
}
