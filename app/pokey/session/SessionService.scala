package pokey.session

import akka.actor.ActorRef
import akka.pattern.ask
import scaldi.{Injectable, Injector}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait SessionService {
  def newSession(): Future[String]

  def isValid(sessionId: String): Future[Boolean]
}

class DefaultSessionService(implicit inj: Injector)
  extends SessionService with Injectable {

  implicit val ec = inject [ExecutionContext]

  private[this] val sessionRegistry = inject [ActorRef] (identified by 'sessionRegistry)
  private[this] implicit val timeout = new akka.util.Timeout(5.seconds)

  def newSession(): Future[String] = (sessionRegistry ? SessionRegistry.NewSession) map {
    case id: String => id
    case other => throw new IllegalStateException("Could not create session")
  }

  def isValid(sessionId: String): Future[Boolean] =
    (sessionRegistry ? SessionRegistry.ValidateSession(sessionId)) map {
      case isValid: Boolean => isValid
      case other => throw new IllegalStateException("Could not determine validity of session")
    }
}
