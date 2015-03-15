package pokey.application

import _root_.play.api.libs.concurrent.Akka
import module.{AkkaModule, ServiceModule, WebModule}
import play.api.GlobalSettings
import play.api.Play.current
import scaldi._
import scaldi.play.ScaldiSupport

object Global extends GlobalSettings with ScaldiSupport {
  override def applicationModule: Injector = (
    new AkkaModule(Akka.system)
      :: new ServiceModule
      :: new WebModule
  )
}
