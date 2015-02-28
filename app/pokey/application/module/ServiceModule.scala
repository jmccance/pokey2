package pokey.application.module

import akka.actor.{ActorRef, ActorSystem}
import pokey.user.{DefaultUserService, UserRegistry, UserService}
import scaldi.Module

class ServiceModule extends Module {
  bind [ActorRef] identifiedBy UserRegistry.identifier to {
    implicit val system = inject [ActorSystem]
    system.actorOf(UserRegistry.props, "user-registry")
  }

  bind [UserService] to new DefaultUserService
}
