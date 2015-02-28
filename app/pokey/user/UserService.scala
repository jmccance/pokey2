package pokey.user

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import pokey.util.Subscribable

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait UserService extends Subscribable[String] {
  def nextUserId(): String

  /**
   * Returns the specified user as well as an ActorRef "socket" for publishing events for that user.
   * Messages sent to the socket will be broadcast to all subscribers to this user.
   *
   * @param id the id of the user
   * @return
   */
  def getUserWithSocket(id: String)
                       (implicit ec: ExecutionContext): Future[(User, ActorRef)]
}

class DefaultUserService(userRegistry: ActorRef) extends UserService {
  implicit val timeout = Timeout(2.seconds)

  override def nextUserId(): String = new java.rmi.server.UID().toString

  override def subscribe(id: String, subscriber: ActorRef): Future[Unit] = ???

  override def unsubscribe(id: String, subscriber: ActorRef): Future[Unit] = ???

  override def getUserWithSocket(id: String)
                                (implicit ec: ExecutionContext): Future[(User, ActorRef)] =
    (userRegistry ? UserRegistry.GetUserForConnection(id)).mapTo[(User, ActorRef)]
}
