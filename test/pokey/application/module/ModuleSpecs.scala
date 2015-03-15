package pokey.application.module

import akka.actor.ActorSystem
import play.api.Configuration
import pokey.assets.controller.AssetController
import pokey.connection.controller.ConnectionController
import pokey.room.actor.RoomProxy
import pokey.room.service.RoomService
import pokey.test.AkkaUnitSpec
import pokey.user.actor.UserProxy
import pokey.user.service.UserService
import scaldi.Injectable._
import scaldi.Module

import concurrent.{ExecutionContext, Future}

class ModuleSpecs extends AkkaUnitSpec {
  "An AkkaModule" should {
    "provide a binding for the ActorSystem passed to the module" in {
      implicit val module = new AkkaModule(system)

      inject [ActorSystem] shouldBe system
    }
  }

  "A WebModule" should {
    val module = new WebModule :: mockServiceModule

    "provide a binding for AssetController" in {
      implicit val m = module
      inject [AssetController]
    }

    "provide a binding for ConnectionController" in {
      implicit val m = module
      inject [ConnectionController]
    }
  }

  "A ServiceModule" should {
    val module = new ServiceModule :: mockDependencies

    "provide a binding for UserService" in {
      implicit val m = module
      inject [UserService]
    }

    "provide a binding for RoomService" in {
      implicit val m = module
      inject [RoomService]
    }
  }

  private[this] def mockServiceModule = new Module {
    bind [UserService] to new UserService {
      override def nextUserId(): String = ???

      override def getUser(id: String)(implicit ec: ExecutionContext): Future[Option[UserProxy]] = ???

      override def createUserForId(id: String)(implicit ec: ExecutionContext): Future[UserProxy] = ???
    }

    bind [RoomService] to new RoomService {
      override def createRoom(ownerId: String)(implicit ec: ExecutionContext): Future[RoomProxy] = ???

      override def getRoom(id: String)(implicit ec: ExecutionContext): Future[Option[RoomProxy]] = ???
    }
  }

  private[this] def mockDependencies = new Module {
    bind [ActorSystem] to system
    bind [Configuration] to Configuration.from(Map(
      "pokey.users.max-idle-time" -> "10s"
    ))
  }

}
