package pokey.user

import akka.actor._

class UserRegistry extends Actor with ActorLogging {
  import pokey.user.UserRegistry._

  def withUsers(users: Map[String, UserProxy]): Receive = {
    case CreateProxyForId(id) if users.contains(id) => sender ! users(id)

    case CreateProxyForId(id) if !users.contains(id) =>
      val user = User(id, "Guest")
      val userProxy =
        UserProxy(user.id, context.actorOf(UserProxyActor.props(user), s"user-proxy-$id"))
      context.watch(userProxy.actor)
      become(users + (id -> userProxy))
      log.info("new_user: {}", user)
      sender ! userProxy

    case GetUserProxy(id) => sender ! users.get(id)

    case Terminated(deadActor) =>
      val deadUser = users.find {
        case (_, proxy) => proxy.actor == deadActor
      }

      deadUser.map {
        case (id, proxy) =>
          log.info("user_pruned: {}", proxy)
          become(users - id)
      }.orElse {
        log.warning("unknown_user_terminated: {}", deadActor)
        None
      }
  }

  def receive = withUsers(Map.empty)

  private[this] def become(users: Map[String, UserProxy]) = context.become(withUsers(users))
}

object UserRegistry {
  /** Identifier for injecting with Scaldi. */
  val identifier = 'userRegistry

  case class CreateProxyForId(id: String)

  case class GetUserProxy(id: String)

  def props = Props(new UserRegistry)
}
