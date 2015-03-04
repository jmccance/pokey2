package pokey.connection.controller

import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import pokey.connection.model
import pokey.connection.model.Event
import pokey.user.actor.UserProxy
import pokey.user.service.UserService

import scala.concurrent.Future

class ConnectionController(userService: UserService,
                           connectionHandlerProps: ConnectionController.HandlerPropsFactory)
  extends Controller {

  private[this] val log = Logger(this.getClass)

  def connect = WebSocket.tryAcceptWithActor[model.Request, Event] { request =>
    log.info("Received WebSocket connection request")
    request.session.get("user_id") match {
      case Some(userId) =>
        userService.createUserForId(userId).map {
          case userProxy => Right(connectionHandlerProps(userProxy))
        }

      case None => Future.successful(Left(Unauthorized("Missing user id")))
    }
  }
}

object ConnectionController {
  type HandlerPropsFactory = ((UserProxy) => WebSocket.HandlerProps)
}
