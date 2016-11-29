package pokey.user.actor

import akka.actor.ActorRef
import pokey.user.model.User

case class UserProxy(id: User.Id, ref: ActorRef)
