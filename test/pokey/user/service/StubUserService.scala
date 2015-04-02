package pokey.user.service

import pokey.user.actor.UserProxy

import concurrent.{ ExecutionContext, Future }

class StubUserService extends UserService {
  private[this] var ids = Stream.from(0)

  override def nextUserId(): String = {
    val next #:: tail = ids
    ids = tail
    next.toString
  }

  override def getUser(id: String)(implicit ec: ExecutionContext): Future[Option[UserProxy]] = ???

  override def createUserForId(id: String)(implicit ec: ExecutionContext): Future[UserProxy] = ???
}
