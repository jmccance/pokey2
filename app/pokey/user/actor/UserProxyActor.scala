package pokey.user.actor

import akka.actor._
import pokey.user.actor.UserProxyActor.Settings
import pokey.user.model.User
import pokey.util.{Subscribable, TopicProtocol}

import concurrent.duration._

class UserProxyActor(settings: Settings, initialUser: User)
  extends Actor
  with ActorLogging
  with Subscribable {

  import UserProxyActor._
  import context.dispatcher

  protected val protocol = UserProxyActor

  private[this] var user: User = initialUser
  private[this] var connections: Set[ActorRef] = Set.empty
  private[this] var oCancellableEviction: Option[Cancellable] = None

  /**
   * When someone subscribes to us, immediately send them the current user state.
   *
   * @param subscriber the subscribing ActorRef
   */
  override def onSubscribe(subscriber: ActorRef) = subscriber ! UserUpdated(user)

  def receive = handleSubscriptions orElse {
    case SetName(name) =>
      user = user.copy(name = name)
      self ! Publish(UserUpdated(user))
      log.info("user_updated: {}", user)

    case NewConnection(conn) =>
      // Attempt to cancel the eviction message. _.cancel() might return false, but that's okay
      // because we'll ignore the message if we have connections anyway.
      oCancellableEviction.foreach(_.cancel())
      oCancellableEviction = None

      // Subscribe the new connection to ourselves so they get updates.
      self ! Subscribe(conn)

      // Watch the connection so we can track our connection count.
      context.watch(conn)

      connections += conn
      log.info("new_connection: {}", conn)

    case EvictUser =>
      if (connections.isEmpty) {
        context.stop(self)
      } else {
        log.warning("eviction_ignored, connection_count: {}", connections.size)
      }

    case Terminated(conn) if connections.contains(conn) =>
      connections = connections - conn
      if (connections.isEmpty) {
        log.info("user_cleanup_scheduled, user_id: {}", user.id)
        oCancellableEviction = Option {
          context.system.scheduler.scheduleOnce(settings.maxIdleDuration, self, EvictUser)
        }
      }
  }
}

object UserProxyActor extends TopicProtocol {
  trait Settings {
    val maxIdleDuration: FiniteDuration
  }

  val defaultSettings = new Settings {
    val maxIdleDuration = 15.seconds
  }

  def props(user: User) = Props(new UserProxyActor(defaultSettings, user))

  case class NewConnection(conn: ActorRef)

  case class SetName(name: String)

  case class UserUpdated(user: User)

  private[actor] case object EvictUser
}
