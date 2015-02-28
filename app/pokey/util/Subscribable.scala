package pokey.util

import akka.actor.ActorRef

trait Subscribable[K] {
  def subscribe(id: K, subscriber: ActorRef): Unit

  def unsubscribe(id: K, subscriber: ActorRef): Unit
}
