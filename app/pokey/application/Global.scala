package pokey.application

import _root_.play.api.libs.concurrent.Akka
import _root_.play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.GlobalSettings
import play.api.Play.current
import pokey.application.module.{DomainModule, WebModule}
import scaldi._
import scaldi.play.ScaldiSupport

object Global extends GlobalSettings with ScaldiSupport {
  implicit lazy val system = Akka.system
  override def applicationModule: Injector = new WebModule :: new DomainModule
}
