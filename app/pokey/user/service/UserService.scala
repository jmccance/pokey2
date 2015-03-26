package pokey.user.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import pokey.user.actor.{ UserProxy, UserRegistry }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait UserService {
  /**
   * @return a new, unique-to-this-instance identifier for a user
   */
  def nextUserId(): String

  def createUserForId(id: String)(implicit ec: ExecutionContext): Future[UserProxy]

  def getUser(id: String)(implicit ec: ExecutionContext): Future[Option[UserProxy]]
}

class DefaultUserService(userRegistry: ActorRef) extends UserService {
  private[this] implicit val timeout = Timeout(2.seconds)

  override def nextUserId(): String = new java.rmi.server.UID().toString

  override def createUserForId(id: String)(implicit ec: ExecutionContext): Future[UserProxy] =
    (userRegistry ? UserRegistry.CreateProxyForId(id)).mapTo[UserProxy]

  override def getUser(id: String)(implicit ec: ExecutionContext): Future[Option[UserProxy]] =
    (userRegistry ? UserRegistry.GetUserProxy(id)).mapTo[Option[UserProxy]]
}
