package pokey.application.module

import akka.actor.{ActorRef, ActorSystem}
import pokey.room.{DefaultRoomService, RoomService}
import pokey.user.{DefaultUserService, UserRegistry, UserService}
import scaldi.Module

class ServiceModule extends Module {
  bind [UserService] to injected [DefaultUserService] (
    'userRegistry -> inject [ActorRef] (identified by UserRegistry.identifier)
  )

  bind [ActorRef] identifiedBy required(UserRegistry.identifier) to {
    implicit val system = inject [ActorSystem]
    system.actorOf(UserRegistry.props, "user-registry")
  }

  bind [RoomService] to injected [DefaultRoomService]
}
