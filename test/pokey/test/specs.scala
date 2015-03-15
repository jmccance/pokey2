package pokey.test

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._
import concurrent.ScalaFutures

trait BaseSpec
  extends WordSpecLike
  with Matchers
  with OptionValues
  with Inspectors
  with ScalaFutures

abstract class UnitSpec extends WordSpec with BaseSpec

abstract class AkkaUnitSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with BaseSpec
  with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  override def afterAll() = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }
}
