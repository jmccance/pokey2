package pokey.connection.controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import pokey.connection.actor.ConnectionHandler
import pokey.connection.model
import model.Event
import play.api.libs.streams.ActorFlow
import pokey.user.service.UserService

import scala.concurrent.Future

class ConnectionController(
  userService: UserService,
  connectionHandlerProps: ConnectionHandler.PropsFactory
)(implicit
  actorSystem: ActorSystem,
  materializer: Materializer)
    extends Controller {

  private[this] val log = Logger(this.getClass)

  def connect = WebSocket.acceptOrResult[model.Command, Event] { request =>
    log.info("Received WebSocket connection request")
    request.session.get("user_id") match {
      case Some(userId) =>
        userService.createUserForId(userId).map { userProxy =>
          Right(ActorFlow.actorRef(connectionHandlerProps(userProxy)))
        }

      case None => Future.successful(Left(Unauthorized("Missing user id")))
    }
  }
}
