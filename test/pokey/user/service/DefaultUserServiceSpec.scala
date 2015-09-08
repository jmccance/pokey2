package pokey.user.service

import pokey.test.AkkaUnitSpec
import pokey.user.actor.{ UserProxy, UserRegistry }

class DefaultUserServiceSpec extends AkkaUnitSpec {
  // In this class, "self" will be playing the role of the UserRegistry.
  import system.dispatcher

  "The DefaultUserServiceSpec" when {
    val someId = "1234"
    val someActorRef = system.deadLetters

    "generating a user id" should {
      "return a String" in {
        newUserService().nextUserId() should not be empty
      }
    }

    "creating a user for an id" should {
      "return the UserProxy provided by the registry" in {
        val service = newUserService()
        val fProxy = service.createUserForId(someId)
        expectMsg(UserRegistry.CreateProxyForId(someId))
        lastSender ! UserProxy(someId, someActorRef)

        whenReady(fProxy) { proxy =>
          proxy.id shouldBe someId
          proxy.ref shouldBe someActorRef
        }
      }
    }

    "getting a user by an id" should {
      "return the UserProxy provided by the registry if it exists" in {
        val service = newUserService()
        val foProxy = service.getUser(someId)

        expectMsg(UserRegistry.GetUserProxy(someId))
        lastSender ! Some(UserProxy(someId, someActorRef))

        whenReady(foProxy) { oProxy =>
          oProxy.value.id shouldBe someId
          oProxy.value.ref shouldBe someActorRef
        }
      }

      "return None if no such user exists" in {
        val service = newUserService()
        val foProxy = service.getUser(someId)

        expectMsg(UserRegistry.GetUserProxy(someId))
        lastSender ! None

        whenReady(foProxy) { oProxy =>
          oProxy shouldBe None
        }
      }
    }
  }

  private[this] def newUserService(): UserService =
    new DefaultUserService(self, pokey.util.uidStream)
}
