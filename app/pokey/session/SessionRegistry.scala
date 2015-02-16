package pokey.session

import akka.actor.{Actor, ActorLogging, Props}
import org.joda.time.DateTime

class SessionRegistry extends Actor with ActorLogging {
  import pokey.session.SessionRegistry._

  def withSessions(sessions: Map[String, Option[DateTime]]): Receive = {
    case NewSession =>
      val id = newSessionId()
      // FIXME Configurable session timeout
      val expiry = DateTime.now().plusMinutes(30)
      context.become(withSessions(sessions + (id -> Some(expiry))))
      sender ! id

    case ValidateSession(id) =>
      val isValid =
        sessions.get(id) match {
          // Such a session exists and it is expired
          case Some(Some(date)) if date.isBeforeNow => false

          // Such a session exists but it is not expired
          case Some(_) => true

          // No such session
          case _ => false
        }

      sender ! isValid

    case ReapSessions =>
      // TODO Notify interested parties when session expires.
      val unexpiredSessions = sessions.filter {
        case (_, Some(expiry)) if expiry.isBeforeNow => false
        case _ => true
      }

      context.become(withSessions(unexpiredSessions))
  }

  def receive = withSessions(Map.empty)

  // FIXME Validate that session ID does not already exist
  private[this] def newSessionId(): String = java.util.UUID.randomUUID().toString
}

object SessionRegistry {
  def props = Props(new SessionRegistry)

  case object NewSession

  case object ReapSessions

  case class ValidateSession(id: String)
}
