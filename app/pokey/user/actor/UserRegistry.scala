package pokey.user.actor

import akka.actor._
import pokey.user.model.User

class UserRegistry(userProxyProps: UserProxyActor.PropsFactory) extends Actor with ActorLogging {
  import UserRegistry._

  private[this] val DefaultName = User.Name.unsafeFrom("Guest")

  def withUsers(users: Map[User.Id, UserProxy]): Receive = {
    case CreateProxyForId(id) if users.contains(id) => sender ! users(id)

    case CreateProxyForId(id) if !users.contains(id) =>
      val user = User(id, DefaultName)
      val userProxy =
        UserProxy(user.id, context.actorOf(userProxyProps(user), s"user-proxy-${id.value}"))
      context.watch(userProxy.ref)
      become(users + (id -> userProxy))
      log.info("new_user: {}", user)
      sender ! userProxy

    case GetUserProxy(id) => sender ! users.get(id)

    case Terminated(deadActor) =>
      val deadUser = users.find {
        case (_, proxy) => proxy.ref == deadActor
      }

      deadUser.foreach {
        case (id, proxy) =>
          log.info("user_pruned: {}", proxy)
          become(users - id)
      }
  }

  def receive = withUsers(Map.empty)

  private[this] def become(users: Map[User.Id, UserProxy]) = context.become(withUsers(users))
}

object UserRegistry {
  case class CreateProxyForId(id: User.Id)

  case class GetUserProxy(id: User.Id)

  def props(userProxyProps: UserProxyActor.PropsFactory) = Props(new UserRegistry(userProxyProps))
}
