package pokey.connection

import akka.actor.ActorRef
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import pokey.connection.ConnectionController.HandlerPropsFactory
import pokey.user.UserService

import scala.concurrent.Future

class ConnectionController(userService: UserService,
                           connectionHandlerProps: HandlerPropsFactory)
  extends Controller {

  private[this] val log = Logger(this.getClass)

  def connect = WebSocket.tryAcceptWithActor[Request, Event] { request =>
    log.info("Received WebSocket connection request")
    request.session.get("user_id") match {
      case Some(userId) =>
        userService.createProxyForId(userId).map {
          case userProxy => Right(connectionHandlerProps(userId, userProxy))
        }

      case None => Future.successful(Left(Unauthorized("Missing user id")))
    }
  }
}

object ConnectionController {
  type HandlerPropsFactory = ((String, ActorRef) => WebSocket.HandlerProps)
}
