package pokey.user

import akka.actor.{Actor, ActorLogging}
import play.api.libs.json.Json

class UserRegistry extends Actor with ActorLogging {
  import pokey.user.UserRegistry._

  def withUsers(users: Map[String, User]): Receive = {
    case Register(user) =>
      log.info("registration: {}", Json.toJson(user))
      context.become(withUsers(users + (user.id -> user)))
  }

  def receive = withUsers(Map.empty)
}

object UserRegistry {
  case class Register(user: User)
}
