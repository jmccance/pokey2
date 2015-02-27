package pokey.util

import akka.actor.ActorRef

import scala.concurrent.Future

trait Subscribable[K] {
  /**
   * Subscribe to change events for the entity with the specified id. If the entity cannot be s
   * ubscribed to, the returned future will fail. Once the Future has returned, the subscriber can
   * expect to receive all change events until they successfully unsubscribe.
   *
   * @param id the id of the entity to which to subscribe
   * @return a Future that will complete successfully or else return an error describing why the
   *         entity could not be subscribed to
   */
  def subscribe(id: K, subscriber: ActorRef): Future[Unit]

  /**
   * Unsubscribe from the specified entity. The subscriber will continue to receive messages until
   * the returned future completes successfully. If the provided subscriber is not actually
   * subscribed the future will complete successfully.
   *
   * @param id the id of the entity from which to unsubscribe
   * @return a Future that will complete successfully or else return an error describing why the
   *         unsubscription attempt failed
   */
  def unsubscribe(id: K, subscriber: ActorRef): Future[Unit]
}
