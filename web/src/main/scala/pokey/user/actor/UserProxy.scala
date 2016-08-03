package pokey.user.actor

import akka.actor.ActorRef

case class UserProxy(id: String, ref: ActorRef)
