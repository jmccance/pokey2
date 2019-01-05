package pokey

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.typesafe.config.Config
import controllers.AssetsComponents
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponents, BuiltInComponentsFromContext}
import play.filters.HttpFiltersComponents
import pokey.application.ApplicationController
import pokey.connection.actor.ConnectionHandler
import pokey.connection.controller.ConnectionController
import pokey.room.actor.RoomRegistry
import pokey.room.model.Room
import pokey.room.service.{DefaultRoomService, RoomService}
import pokey.user.actor.{UserProxyActor, UserRegistry}
import pokey.user.model.User
import pokey.user.service.{DefaultUserService, UserService}

import scala.concurrent.duration.{FiniteDuration, _}

class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = new AppComponents(context).application
}

class AppComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
  with AssetsComponents
  with ConfigComponents
  with HttpFiltersComponents
  with ServiceComponents
  with WebComponents

trait WebComponents { this: AssetsComponents with ConfigComponents with ServiceComponents with BuiltInComponentsFromContext =>
  private[this] implicit lazy val implicitActorSystem: ActorSystem = actorSystem

  lazy val appController: ApplicationController = {
    val settings =
      ApplicationController.Settings.from(pokeyConfig).fold(
        identity,
        errors => sys.error(errors.toString))

    new ApplicationController(assets, controllerComponents, settings, userService)
  }

  lazy val connectionController: ConnectionController = {
    val settings = ConnectionHandler.Settings(
      pokeyConfig.getDuration("connection.heartbeat-interval", TimeUnit.MILLISECONDS).millis)

    new ConnectionController(controllerComponents, userService, ConnectionHandler.propsFactory(roomService, settings))
  }

  def router: Router = new AppRouter(appController, connectionController)
}

trait ServiceComponents { this: BuiltInComponents with ConfigComponents =>
  import pokey.util.uidStream

  lazy val userService: UserService = {
    val userIdStream = uidStream.map(User.Id.unsafeFrom)

    val userRegistryRef = {
      val settings = new UserProxyActor.Settings {
        override val maxIdleDuration: FiniteDuration =
          pokeyConfig.getDuration("users.max-idle-time", TimeUnit.MILLISECONDS).millis
      }

      val userProxyProps = UserProxyActor.propsFactory(settings)

      actorSystem.actorOf(UserRegistry.props(userProxyProps), "user-registry")
    }

    new DefaultUserService(userRegistryRef, userIdStream)
  }

  lazy val roomService: RoomService = {
    val roomRegistryRef = {
      val roomIdStream = uidStream.map(Room.Id.unsafeFrom)
      actorSystem.actorOf(RoomRegistry.props(roomIdStream, userService), "room-registry")
    }

    new DefaultRoomService(roomRegistryRef)
  }
}

trait ConfigComponents { this: BuiltInComponents =>
  lazy val pokeyConfig: Config = configuration.underlying.getConfig("pokey")
}