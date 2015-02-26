package pokey.connection

import akka.actor.ActorRef
import play.api.mvc._
import play.api.{Application, Logger}
import pokey.session.SessionService
import scaldi.{Injectable, Injector}

import scala.concurrent.{ExecutionContext, Future}

class ConnectionController(implicit inj: Injector) extends Controller with Injectable {
  private[this] val log = Logger(this.getClass)

  private[this] val sessionService = inject [SessionService]
  private[this] implicit val executionContext = inject [ExecutionContext]
  private[this] implicit val application = inject [Application]

  def connect = WebSocket.tryAcceptWithActor[Request, Response] { request =>
    log.info("Received WebSocket connection request")
    val oSessionId = request.session.get("session_id")
    oSessionId match {
      case Some(sessionId) =>
        sessionService.isValid(sessionId).map {
          case true => Right(ConnectionHandler.props(sessionId, _: ActorRef))
          case false => Left(Unauthorized("Invalid session id"))
        }

      case None => Future.successful(Left(Unauthorized("Invalid session id")))
    }

  }
}
