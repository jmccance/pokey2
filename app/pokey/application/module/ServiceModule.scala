package pokey.application.module

import akka.actor.{ActorRef, ActorSystem}
import pokey.room.{DefaultRoomService, RoomRegistry, RoomService}
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

  bind [RoomService] to injected [DefaultRoomService] (
    'roomRegistry -> inject [ActorRef] (identified by RoomRegistry.identifier)
  )

  bind [ActorRef] identifiedBy required(RoomRegistry.identifier) to {
    implicit val system = inject [ActorSystem]
    val userService = inject [UserService]

    system.actorOf(RoomRegistry.props(userService), "room-registry")
  }
}
