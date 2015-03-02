package pokey.user

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait UserService {
  /**
   * @return a new, unique-to-this-instance identifier for a user
   */
  def nextUserId(): String

  def createProxyForId(id: String)
                      (implicit ec: ExecutionContext): Future[ActorRef]

  def getUserProxy(id: String)
                  (implicit ec: ExecutionContext): Future[Option[ActorRef]]
}

class DefaultUserService(userRegistry: ActorRef) extends UserService {
  private[this] implicit val timeout = Timeout(2.seconds)

  override def nextUserId(): String = new java.rmi.server.UID().toString


  override def createProxyForId(id: String)
                               (implicit ec: ExecutionContext): Future[ActorRef] =
    (userRegistry ? UserRegistry.CreateProxyForId(id)).mapTo[ActorRef]

  override def getUserProxy(id: String)
                           (implicit ec: ExecutionContext): Future[Option[ActorRef]] =
    (userRegistry ? UserRegistry.GetUserProxy(id)).mapTo[Option[ActorRef]]
}
