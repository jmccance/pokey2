package pokey.user

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import pokey.util.Subscribable

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait UserService extends Subscribable[String] {
  /**
   * @return a new, unique-to-this-instance identifier for a user
   */
  def nextUserId(): String

  /**
   * Returns the specified user as well as an ActorRef "socket" for publishing events for that user.
   * Messages sent to the socket will be broadcast to all subscribers to this user.
   *
   * @param id the id of the user
   * @return
   */
  def startConnection(id: String)(implicit ec: ExecutionContext): Future[User]
  
  def endConnection(id: String): Unit

  def setName(id: String, name: String): Unit
}

class DefaultUserService(userRegistry: ActorRef) extends UserService {
  implicit val timeout = Timeout(2.seconds)

  override def nextUserId(): String = new java.rmi.server.UID().toString

  override def startConnection(id: String)(implicit ec: ExecutionContext): Future[User] =
    (userRegistry ? UserRegistry.StartConnection(id)).mapTo[User]
  
  override def endConnection(id: String): Unit = userRegistry ! UserRegistry.EndConnection(id)

  override def subscribe(id: String, subscriber: ActorRef): Unit =
    userRegistry ! UserRegistry.Subscribe(id, subscriber)

  override def unsubscribe(id: String, subscriber: ActorRef): Unit =
    userRegistry ! UserRegistry.Unsubscribe(id, subscriber)

  override def setName(id: String, name: String): Unit =
    userRegistry ! UserRegistry.SetName(id, name)
}
