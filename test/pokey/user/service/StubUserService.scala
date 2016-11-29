package pokey.user.service

import pokey.user.actor.UserProxy
import pokey.user.model.User

import scala.concurrent.{ExecutionContext, Future}

class StubUserService extends UserService {
  private[this] var ids = Stream.from(0)

  override def nextUserId(): User.Id = {
    val next #:: tail = ids
    ids = tail
    User.Id.unsafeFrom(next.toString)
  }

  override def getUser(id: User.Id)(implicit ec: ExecutionContext): Future[Option[UserProxy]] = ???

  override def createUserForId(id: User.Id)(implicit ec: ExecutionContext): Future[UserProxy] = ???
}
