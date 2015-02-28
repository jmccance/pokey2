package pokey.user

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pokey.util.Publisher

class UserRegistry extends Actor with ActorLogging {
  import pokey.user.UserRegistry._
  import pokey.user.events._

  private[this] case class MetaUser(user: User, publisher: ActorRef, connectionCount: Int) {
    def incrementConnectionCount() = this.copy(connectionCount = connectionCount + 1)
    def decrementConnectionCount() = this.copy(connectionCount = connectionCount - 1)
  }

  def withRecords(records: Map[String, MetaUser]): Receive = {
    case Subscribe(id, subscriber) =>
      log.info("subscribe: (id: {}, subscriber: {})", id, subscriber)
      records(id).publisher ! Publisher.Subscribe(subscriber)

    case StartConnection(id) if records.contains(id) =>
      val metaUser = records(id).incrementConnectionCount()
      context.become(withRecords(records + (id -> metaUser)))
      sender ! records(id).user

    case StartConnection(id) if !records.contains(id) =>
      val user = User(id, "J. Doe")
      val publisher = context.actorOf(Publisher.props)
      context.become(withRecords(records + (id -> MetaUser(user, publisher, 0))))
      sender ! user

    case EndConnection(id) if records.contains(id) =>
      val metaUser = records(id).decrementConnectionCount()
      context.become(withRecords(records + (id -> metaUser)))
      if (metaUser.connectionCount <= 0) {
        log.info("user_cleanup_scheduled, user_id: {}", id)
        // TODO Schedule user cleanup after last connection is terminated
        // TODO Capture scheduled event so we can cancel if user reconnects before the timeout.
      }

    case SetName(id, name) =>
      log.info("set_name: (id: {}, name: {})", id, name)

      val metaUser = records(id)
      val user = metaUser.user.copy(name = name)
      val updatedMetaUser = metaUser.copy(user = user)

      context.become(withRecords(records + (id -> updatedMetaUser)))
      metaUser.publisher ! UserUpdated(user)
  }

  def receive = withRecords(Map.empty)
}

object UserRegistry {
  /** Identifier for injecting with Scaldi. */
  val identifier = 'userRegistry

  case class StartConnection(id: String)

  case class EndConnection(id: String)
  
  case class Subscribe(id: String, subscriber: ActorRef)

  case class Unsubscribe(id: String, subscriber: ActorRef)

  case class SetName(id: String, name: String)

  def props = Props(new UserRegistry)
}
