package pokey.application

import _root_.akka.actor.ActorSystem
import _root_.play.api.libs.concurrent.Akka
import play.api.GlobalSettings
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits
import pokey.application.module.{ServiceModule, WebModule}
import scaldi._
import scaldi.play.ScaldiSupport

import scala.concurrent.ExecutionContext

object Global extends GlobalSettings with ScaldiSupport {
  override def applicationModule: Injector = {
    val contextModule = new Module {
      bind [ActorSystem] to Akka.system
      bind [ExecutionContext] to Implicits.defaultContext
    }

    contextModule :: new ServiceModule :: new WebModule
  }
}
