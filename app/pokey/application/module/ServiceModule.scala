package pokey.application.module

import akka.actor.{ActorRef, ActorSystem}
import pokey.user.{DefaultUserService, UserRegistry, UserService}
import scaldi.Module

class ServiceModule extends Module {
  bind [ActorRef] identifiedBy required(UserRegistry.identifier) to {
    implicit val system = inject [ActorSystem]
    system.actorOf(UserRegistry.props, "user-registry")
  }

  bind [UserService] to injected [DefaultUserService] (
    'userRegistry -> inject [ActorRef] (identified by UserRegistry.identifier)
  )
}
