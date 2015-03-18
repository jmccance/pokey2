package pokey.application.module

import akka.actor.{ActorRef, ActorSystem}
import play.api.Configuration
import pokey.room.actor.RoomRegistry
import pokey.room.service.{DefaultRoomService, RoomService}
import pokey.user.actor.{UserProxyActor, UserRegistry}
import pokey.user.service.{DefaultUserService, UserService}
import pokey.util.uidStream
import scaldi.Module

import scala.concurrent.duration._

class ServiceModule extends Module {
  bind [Stream[String]] toProvider uidStream
  bind [UserService] to injected [DefaultUserService] (
    'userRegistry -> inject [ActorRef] (identified by UserRegistry.identifier)
  )

  bind[UserProxyActor.PropsFactory] to {
    val config = inject [Configuration]
    val settings = new UserProxyActor.Settings {
      override val maxIdleDuration = config.getMilliseconds("pokey.users.max-idle-time").get.millis
    }

    UserProxyActor.propsFactory(settings)
  }

  bind [ActorRef] identifiedBy required(UserRegistry.identifier) to {
    implicit val system = inject [ActorSystem]
    val userProxyProps = inject [UserProxyActor.PropsFactory]

    system.actorOf(UserRegistry.props(userProxyProps), "user-registry")
  }

  bind [RoomService] to injected [DefaultRoomService] (
    'roomRegistry -> inject [ActorRef] (identified by RoomRegistry.identifier)
  )

  bind [ActorRef] identifiedBy required(RoomRegistry.identifier) to {
    implicit val system = inject [ActorSystem]
    val userService = inject [UserService]
    val idStream = inject [Stream[String]]

    system.actorOf(RoomRegistry.props(idStream, userService), "room-registry")
  }

}
