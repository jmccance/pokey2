package pokey.user

import akka.actor.ActorRef

case class UserProxy(id: String, actor: ActorRef)
