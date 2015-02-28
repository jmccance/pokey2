package pokey.connection

import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import pokey.user.UserService

import scala.concurrent.Future

class ConnectionController(userService: UserService,
                           connectionHandlerProps: (String => WebSocket.HandlerProps))
  extends Controller {

  private[this] val log = Logger(this.getClass)

  def connect = WebSocket.tryAcceptWithActor[Request, Response] { request =>
    log.info("Received WebSocket connection request")
    request.session.get("user_id") match {
      case Some(userId) =>
        userService.startConnection(userId).map { user =>
          Right(connectionHandlerProps(userId))
        }

      case None => Future.successful(Left(Unauthorized("Missing user id")))
    }
  }
}
