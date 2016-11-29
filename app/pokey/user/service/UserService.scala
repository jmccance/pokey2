package pokey.user.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import pokey.user.actor.{UserProxy, UserRegistry}
import pokey.user.model.User

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait UserService {
  /**
   * @return a new, unique-to-this-instance identifier for a user
   */
  def nextUserId(): User.Id

  def createUserForId(id: User.Id)(implicit ec: ExecutionContext): Future[UserProxy]

  def getUser(id: User.Id)(implicit ec: ExecutionContext): Future[Option[UserProxy]]
}

class DefaultUserService(
    userRegistry: ActorRef,
    private[this] var ids: Stream[User.Id]
) extends UserService {
  private[this] implicit val timeout = Timeout(2.seconds)

  override def nextUserId(): User.Id = ids.synchronized {
    val id #:: rest = ids
    ids = rest
    id
  }

  override def createUserForId(id: User.Id)(implicit ec: ExecutionContext): Future[UserProxy] =
    (userRegistry ? UserRegistry.CreateProxyForId(id)).mapTo[UserProxy]

  override def getUser(id: User.Id)(implicit ec: ExecutionContext): Future[Option[UserProxy]] =
    (userRegistry ? UserRegistry.GetUserProxy(id)).mapTo[Option[UserProxy]]
}
