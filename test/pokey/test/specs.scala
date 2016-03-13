package pokey.test

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest._
import concurrent.ScalaFutures
import mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
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

  implicit class RichTestProbe(probe: TestProbe) {
    def expectMsgEventually[T](o: T): T = {
      val firstMatch = probe.fishForMessage() {
        case msg if msg == o => true
        case _ => false
      }

      // firstMatch == o, so firstMatch is a T, right?
      firstMatch.asInstanceOf[T]
    }
  }

  override def afterAll() = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }
}

abstract class PlayUnitSpec
  extends PlaySpec
  with BaseSpec
  with Results
