package pokey.connection.controller

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import pokey.connection.actor.ConnectionHandler
import pokey.connection.model
import pokey.connection.model.Event
import pokey.user.model.User
import pokey.user.service.UserService

import scala.concurrent.Future

class ConnectionController(
  cc: ControllerComponents,
  userService: UserService,
  connectionHandlerProps: ConnectionHandler.PropsFactory)(implicit
  actorSystem: ActorSystem,
  materializer: Materializer)
  extends AbstractController(cc) {

  private[this] val log = Logger(this.getClass)
  private[this] implicit lazy val executionContext = defaultExecutionContext

  def connect: WebSocket = WebSocket.acceptOrResult[model.Command, Event] { request =>
    log.info("Received WebSocket connection request")
    request.session.get("user_id").flatMap(User.Id.from) match {
      case Some(userId) =>
        userService.createUserForId(userId).map { userProxy =>
          Right(ActorFlow.actorRef(connectionHandlerProps(userProxy)))
        }

      case None => Future.successful(Left(Unauthorized("Missing user id")))
    }
  }
}
