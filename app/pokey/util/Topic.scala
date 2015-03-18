package pokey.util

import akka.actor._

trait TopicProtocol {
  case class Subscribe(subscriber: ActorRef)

  case class Subscribed(subscriber: ActorRef)

  case class Unsubscribe(subscriber: ActorRef)

  case class Unsubscribed(subscriber: ActorRef)

  case class Publish(message: Any)
}

class Topic private(subscribers: Set[ActorRef]) {
  def subscribe(subscriber: ActorRef): Topic = new Topic(subscribers + subscriber)

  def unsubscribe(subscriber: ActorRef): Topic = new Topic(subscribers - subscriber)

  def publish(message: Any): Unit = subscribers.foreach(_ ! message)
}

object Topic {
  def apply() = new Topic(Set.empty)
}

trait Subscribable {
  this: Actor =>

  protected val protocol: TopicProtocol
  import protocol._

  private[this] var topic: Topic = Topic()

  final def handleSubscriptions(implicit ctx: ActorContext): Actor.Receive = {
    case Subscribe(subscriber) =>
      topic = topic.subscribe(subscriber)
      onSubscribe(subscriber)
      sender ! Subscribed(subscriber)

    case Unsubscribe(subscriber) =>
      topic = topic.unsubscribe(subscriber)
      onUnsubscribe(subscriber)
      sender ! Unsubscribed(subscriber)

    case Publish(message) if sender == self => topic.publish(message)
  }

  // $COVERAGE-OFF$
  // No need to test the stub implementations
  def onSubscribe(subscriber: ActorRef): Unit = ()
  def onUnsubscribe(subscriber: ActorRef): Unit = ()
  // $COVERAGE-ON$
}
