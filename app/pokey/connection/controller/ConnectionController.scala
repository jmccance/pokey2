package pokey.connection.controller

import play.api.{ Application, Logger }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._
import pokey.connection.actor.ConnectionHandler
import pokey.connection.model
import model.Event
import pokey.user.service.UserService

import scala.concurrent.Future

class ConnectionController(userService: UserService,
                           connectionHandlerProps: ConnectionHandler.PropsFactory)(implicit app: Application)
    extends Controller {

  private[this] val log = Logger(this.getClass)

  implicit val materializer = app.materializer

  implicit val commandEventFlowTransformer =
    MessageFlowTransformer.jsonMessageFlowTransformer[model.Command, Event]

  def connect = WebSocket.tryAcceptWithActor[model.Command, Event] { request =>
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
