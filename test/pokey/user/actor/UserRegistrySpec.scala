package pokey.user.actor

import akka.pattern.ask
import akka.testkit.EventFilter
import akka.util.Timeout
import pokey.test.AkkaUnitSpec
import pokey.user.actor.UserRegistry.{CreateProxyForId, GetUserProxy}
import pokey.user.model.User

import scala.concurrent.duration._

class UserRegistrySpec extends AkkaUnitSpec {
  val existingUserId = User.Id.unsafeFrom("1234")
  val newUserId = User.Id.unsafeFrom("5678")

  "A UserRegistry" when {

    "receiving a CreateProxyForId message for a new id" should {
      "create a new UserProxy and reply with it" in {
        val registry = init()
        registry ! CreateProxyForId(newUserId)
        expectMsgType[UserProxy]
      }
    }

    "receiving a CreateProxyForId message for an existing id" should {
      "reply with the existing UserProxy" in {
        val registry = init()
        registry ! CreateProxyForId(existingUserId)
        expectMsgType[UserProxy]
      }
    }

    "receiving a GetUserProxy message for an existing id" should {
      "reply with the UserProxy" in {
        val registry = init()
        registry ! GetUserProxy(existingUserId)
        expectMsgPF() {
          case msg =>
            msg shouldBe a[Some[_]]
            val Some(UserProxy(id, _)) = msg
            id shouldBe existingUserId
        }
      }
    }

    "receiving a GetUserProxy message for a non-existing id" should {
      "reply with None" in {
        val registry = init()
        registry ! GetUserProxy(newUserId)
        expectMsgPF() {
          case msg => msg shouldBe None
        }
      }
    }

    "a user proxy terminates" should {
      "remove the user from the registry" in {
        implicit val timeout = new Timeout(2.seconds)
        val registry = init()
        val foProxy = (registry ? GetUserProxy(existingUserId)).mapTo[Some[UserProxy]]
        whenReady(foProxy) {
          case Some(proxy) =>
            EventFilter.info(message = s"user_pruned: $proxy") intercept {
              system.stop(proxy.ref)
            }
        }
      }
    }

    def init() = {
      val settings = new UserProxyActor.Settings {
        override val maxIdleDuration: FiniteDuration = 10.minutes
      }

      val registry = system.actorOf(
        UserRegistry.props(UserProxyActor.props(settings, _)),
        s"user-registry-${java.util.UUID.randomUUID()}"
      )

      EventFilter.info(pattern = "^new_user.*", occurrences = 1) intercept {
        registry ! CreateProxyForId(existingUserId)
        expectMsgClass(classOf[UserProxy])
      }
      registry
    }
  }
}
