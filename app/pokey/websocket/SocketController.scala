package pokey.websocket

import akka.actor.ActorRef
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc.WebSocket.FrameFormatter._
import play.api.mvc._
import play.api.{Application, Logger}
import pokey.session.SessionService
import scaldi.{Injectable, Injector}

import scala.concurrent.{ExecutionContext, Future}

class SocketController(implicit inj: Injector) extends Controller with Injectable {
  private[this] val log = Logger(this.getClass)

  private[this] val sessionService = inject [SessionService]
  private[this] implicit val executionContext = inject [ExecutionContext]
  private[this] implicit val application = inject [Application]

  implicit val requestFrameFormatter: FrameFormatter[Request] = jsonFrame[Request]
  implicit val responseFrameFormatter: FrameFormatter[Response] = jsonFrame[Response]

  def socket = WebSocket.tryAcceptWithActor[Request, Response] { request =>
    log.info("Received attempted websocket connection")
    val oSessionId = request.session.get("session_id")
    oSessionId match {
      case Some(sessionId) =>
        sessionService.isValid(sessionId).map {
          case true => Right(SocketHandler.props(sessionId, _: ActorRef))
          case false => Left(Unauthorized("Invalid session id"))
        }
      case None => Future.successful(Left(Unauthorized("No session id specified")))
    }

  }
}
