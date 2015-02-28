package pokey.user

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import pokey.util.Publisher

class UserRegistry extends Actor with ActorLogging {
  import pokey.user.UserRegistry._
  import pokey.user.events._

  private[this] case class Record(user: User, publisher: ActorRef)

  def withRecords(records: Map[String, Record]): Receive = {
    case Subscribe(id, subscriber) =>
      log.info("subscribe: (id: {}, subscriber: {})", id, subscriber)
      records(id).publisher ! Publisher.Subscribe(subscriber)

    case NewConnection(id) if records.contains(id) =>
      sender ! records(id).user

    case NewConnection(id) if !records.contains(id) =>
      val user = User(id, "J. Doe")
      val publisher = context.actorOf(Publisher.props)
      context.become(withRecords(records + (id -> Record(user, publisher))))
      sender ! user

    case SetName(id, name) =>
      log.info("set_name: (id: {}, name: {})", id, name)
      val Record(user, publisher) = records(id)
      val updatedUser = user.copy(name = name)
      context.become(withRecords(records + (id -> Record(updatedUser, publisher))))
      publisher ! UserUpdated(updatedUser)
  }

  def receive = withRecords(Map.empty)
}

object UserRegistry {
  /** Identifier for injecting with Scaldi. */
  val identifier = 'userRegistry

  case class Subscribe(id: String, subscriber: ActorRef)

  case class Unsubscribe(id: String, subscriber: ActorRef)

  case class NewConnection(id: String)

  case class SetName(id: String, name: String)

  def props = Props(new UserRegistry)
}
