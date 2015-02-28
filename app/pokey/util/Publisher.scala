package pokey.util

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Publisher extends Actor with ActorLogging {
  import pokey.util.Publisher._

  def withSubscribers(subscribers: Set[ActorRef]): Receive = {
    case Subscribe(subscriber) =>
      context.become(withSubscribers(subscribers + subscriber))
      sender ! Subscribed(subscriber)

    case Unsubscribe(subscriber) =>
      context.become(withSubscribers(subscribers - subscriber))
      sender ! Unsubscribed(subscriber)

    case message => subscribers.foreach(_ ! message)
  }

  def receive = withSubscribers(Set.empty)
}

object Publisher {
  case class Subscribe(subscriber: ActorRef)

  case class Subscribed(subscriber: ActorRef)

  case class Unsubscribe(subscriber: ActorRef)

  case class Unsubscribed(subscriber: ActorRef)

  def props = Props(new Publisher)
}
