package pokey.util

import akka.actor._

trait TopicProtocol {
  case class Subscribe(subscriber: ActorRef)

  case class Subscribed(subscriber: ActorRef)

  case class Unsubscribe(subscriber: ActorRef)

  case class Unsubscribed(subscriber: ActorRef)
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
  protected val protocol: TopicProtocol
  private[this] var topic: Topic = Topic()
  import protocol._

  def handleSubscriptions: Actor.Receive = {
    case Subscribe(subscriber) =>
      topic = topic.subscribe(subscriber)

    case Unsubscribe(subscriber) =>
      topic = topic.unsubscribe(subscriber)
  }

  def onSubscribe(subscriber: ActorRef): Unit = ()
  def onUnsubscribe(subscriber: ActorRef): Unit = ()
  def publish(message: Any): Unit = topic.publish(message)
}
