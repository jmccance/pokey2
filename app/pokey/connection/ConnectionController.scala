package pokey.connection

import akka.actor.ActorRef
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import pokey.user.UserService

import scala.concurrent.Future

class ConnectionController(userService: UserService) extends Controller {
  private[this] val log = Logger(this.getClass)

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
