package pokey.user.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import pokey.test.AkkaUnitSpec
import pokey.user.actor.UserProxyActor._
import pokey.user.model.User

import concurrent.duration._

class UserProxyActorSpec extends AkkaUnitSpec {
  val user = User("U-1", "Felmet")


  "A UserProxyActor" when {
    "it receives a SetName message" should {
      "publish the new user state" in withProxyActor() { upa =>
        upa ! Subscribe(self)
        expectMsg(UserUpdated(user))
        expectMsg(Subscribed(self))

        upa ! SetName("Rincewind")
        expectMsg(UserUpdated(user.copy(name = "Rincewind")))
      }
    }

    "its last connection terminates" should {
      "terminate after the configured maxIdleDuration if no new connections are made" in
        withProxyActor(500.millis) { upa =>
          watch(upa)

          val conn = TestProbe()
          upa ! NewConnection(conn.ref)
          system.stop(conn.ref)

          within(500.millis, 1.second) {
            expectTerminated(upa)
          }
        }

      "not terminate if a new connection is made" in pending
    }
  }

  def withProxyActor(_maxIdleDuration: FiniteDuration = 1.day) (testcode: ActorRef => Unit) = {
    val settings = new UserProxyActor.Settings {
      override val maxIdleDuration: FiniteDuration = _maxIdleDuration
    }
    val upa = system.actorOf(UserProxyActor.props(settings, user))
    testcode(upa)
  }
}
