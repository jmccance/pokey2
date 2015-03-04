package pokey.user

import akka.actor._
import pokey.util.{Subscribable, TopicProtocol}

class UserProxyActor(initialUser: User) extends Actor with ActorLogging with Subscribable {
  import pokey.user.UserProxyActor._

  protected val protocol = UserProxyActor

  private[this] var user: User = initialUser
  private[this] var connections: Set[ActorRef] = Set.empty

  /**
   * When someone subscribes to us, immediately send them the current user state.
   *
   * @param subscriber the subscribing ActorRef
   */
  override def onSubscribe(subscriber: ActorRef) = subscriber ! user

  def receive = handleSubscriptions orElse {
    case SetName(name) =>
      user = user.copy(name = name)
      self ! Publish(UserUpdated(user))
      log.info("user_updated: {}", user)

    case NewConnection(conn) =>
      self ! Subscribe(conn)
      context.watch(conn)
      connections += conn
      log.info("new_connection: {}", conn)

    case Terminated(conn) if connections.contains(conn) =>
      connections = connections - conn
      if (connections.isEmpty) {
        log.info("(NYI) user_cleanup_scheduled, user_id: {}", user.id)
        // TODO Schedule user cleanup after last connection is terminated
        // TODO Capture scheduled event so we can cancel if user reconnects before the timeout.
      }
  }
}

object UserProxyActor extends TopicProtocol {
  def props(user: User) = Props(new UserProxyActor(user))

  case class NewConnection(conn: ActorRef)

  case class SetName(name: String)

  case class UserUpdated(user: User)
}
