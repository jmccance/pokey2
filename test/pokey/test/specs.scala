package pokey.test

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._
import concurrent.ScalaFutures
import mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results

trait BaseSpec
  extends WordSpecLike
  with OptionValues
  with Inspectors
  with ScalaFutures
  with MockitoSugar

abstract class UnitSpec extends WordSpec with BaseSpec with Matchers

abstract class AkkaUnitSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with BaseSpec
  with Matchers
  with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  override def afterAll() = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }
}

abstract class PlayUnitSpec
  extends PlaySpec
  with BaseSpec
  with Results
