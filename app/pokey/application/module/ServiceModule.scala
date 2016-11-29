package pokey.application.module

import akka.actor.{ActorRef, ActorSystem}
import play.api.Configuration
import pokey.room.actor.RoomRegistry
import pokey.room.model.Room
import pokey.room.service.{DefaultRoomService, RoomService}
import pokey.user.actor.{UserProxyActor, UserRegistry}
import pokey.user.model.User
import pokey.user.service.{DefaultUserService, UserService}
import pokey.util.uidStream
import scaldi.Module

import scala.concurrent.duration._

class ServiceModule extends Module {
  // uidStream is guaranteed to return non-empty strings, so we can safely build our Id streams with
  // unsafeFrom.

  bind[Stream[User.Id]] toProvider uidStream.map(User.Id.unsafeFrom)
  bind[Stream[Room.Id]] toProvider uidStream.map(Room.Id.unsafeFrom)

  bind[UserService] to injected[DefaultUserService](
    'userRegistry -> inject[ActorRef](identified by UserRegistry.identifier)
  )

  bind[UserProxyActor.PropsFactory] to {
    val config = inject[Configuration]
    val settings = new UserProxyActor.Settings {
      override val maxIdleDuration: FiniteDuration =
        config.getMilliseconds("pokey.users.max-idle-time").get.millis
    }

    UserProxyActor.propsFactory(settings)
  }

  bind[ActorRef] identifiedBy required(UserRegistry.identifier) to {
    implicit val system = inject[ActorSystem]
    val userProxyProps = inject[UserProxyActor.PropsFactory]

    system.actorOf(UserRegistry.props(userProxyProps), "user-registry")
  }

  bind[RoomService] to injected[DefaultRoomService](
    'roomRegistry -> inject[ActorRef](identified by RoomRegistry.identifier)
  )

  bind[ActorRef] identifiedBy required(RoomRegistry.identifier) to {
    implicit val system = inject[ActorSystem]
    val userService = inject[UserService]
    val roomIdStream = inject[Stream[Room.Id]]

    system.actorOf(RoomRegistry.props(roomIdStream, userService), "room-registry")
  }

}
