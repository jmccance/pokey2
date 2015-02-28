package pokey.user

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pokey.util.Publisher

class UserRegistry extends Actor with ActorLogging {
  import pokey.user.UserRegistry._

  def withUsers(users: Map[String, (User, ActorRef)]): Receive = {
    case GetUserForConnection(id) if users.contains(id) =>
      sender ! users(id)

    case GetUserForConnection(id) if !users.contains(id) =>
      val user = User(id, "J. Doe")
      val publisher = context.actorOf(Publisher.props)
      context.become(withUsers(users + (id -> (user, publisher))))
      sender ! (user, publisher)
  }

  def receive = withUsers(Map.empty)
}

object UserRegistry {
  /** Identifier for injecting with Scaldi. */
  val identifier = 'userRegistry

  case class GetUserForConnection(id: String)

  def props = Props(new UserRegistry)
}
