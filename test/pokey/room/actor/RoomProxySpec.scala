package pokey.room.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import pokey.room.actor.RoomProxyActor._
import pokey.room.model.Room
import pokey.test.AkkaUnitSpec
import pokey.user.actor.{UserProxy, UserProxyActor}
import pokey.user.model.User

class RoomProxySpec extends AkkaUnitSpec {
  val owner = User("U-1", "Esme")
  val someUser = User("U-2", "Magrat")
  val anotherUser = User("U-3", "Nanny")
  val roomId = "R-1"
  val room = Room("R-1", owner.id)

  "A RoomProxy" when {
    "it receives a JoinRoom message from a connection" should {
      "subscribe to the supplied UserProxy, subscribe the connection to itself, and publish a " +
        "UserJoined Message" in withContext {
        case Context(rpa, ownerProbe, conn, _) =>
          val userProbe = TestProbe()
          val userProxy = UserProxy(someUser.id, userProbe.ref)

          rpa ! JoinRoom(userProxy)

          userProbe.expectMsg(UserProxyActor.Subscribe(rpa))
          userProbe.send(rpa, UserProxyActor.UserUpdated(someUser))

          expectMsgType[RoomUpdated]
          expectMsg(UserJoined(roomId, someUser))
      }
    }

    "it receives a UserUpdated message for a member of the room" should {
      "publish the update to the room" in pendingUntilFixed(withContextWithUsers(someUser, anotherUser) { ctx =>
        val connP = ctx.users(anotherUser.id).connP
        val updatedUser = someUser.copy(name = "Verence")
        ctx.users(someUser.id).proxyP.send(ctx.rpa, UserProxyActor.UserUpdated(updatedUser))
        connP.expectMsg(UserProxyActor.UserUpdated(updatedUser))
      })
    }

    "it receives a LeaveRoom message" which {
      "has a UserProxy that is a member" should {
        "unsubscribe from the UserProxy" in pendingUntilFixed(withContextWithUsers(someUser, anotherUser) { ctx =>
          val someUserCtx = ctx.users(someUser.id)
          someUserCtx.connP.ignoreNoMsg()
          val anotherUserCtx = ctx.users(anotherUser.id)
          anotherUserCtx.connP.send(ctx.rpa, RoomProxyActor.LeaveRoom(anotherUserCtx.proxy))

          someUserCtx.connP.expectMsg(UserLeft(roomId, anotherUserCtx.user))
        })

        "unsubscribe the connection from itself" in pending

        "publish UserLeft message to the room" in pending
      }
    }

    "it receives a SubmitEstimate message from a connection" should {
      "send an EstimateUpdated out to members of the room" in pending
    }

    "it receives a RevealFor message for a userId" which {
      "is allowed to reveal the room" should {
        "send a Revealed message to the room" in pending
      }

      "is not allowed to reveal the room" should {
        "forward the error back to the connection" in pending
      }
    }

    "it receives a ClearFor message for a userId" which {
      "is allowed to Clear the room" should {
        "send a Cleared message to the room" in pending
      }

      "is not allowed to Clear the room" should {
        "forward the error back to the connection" in pending
      }
    }

    "the room's owner terminates" should {
      "publish a room closed message and self-terminate" in withContext {
        case Context(rpa, ownerProxy, conn, _) =>
          val probe = TestProbe()
          val connProbe = TestProbe()
          val memberProbe = TestProbe()
          probe.watch(rpa)

          connProbe.send(rpa, JoinRoom(UserProxy(owner.id, memberProbe.ref)))
          memberProbe.expectMsgType[RoomProxyActor.Subscribe]
          memberProbe.send(rpa, RoomProxyActor.Subscribed(rpa))
          connProbe.expectMsgType[RoomUpdated]

          system.stop(ownerProxy.ref)

          connProbe.expectMsg(Closed(roomId))
          probe.expectTerminated(rpa)
      }
    }
  }

  case class Context(rpa: ActorRef,
                     ownerP: TestProbe,
                     connP: TestProbe,
                     users: Map[String, UserContext] = Map.empty)
  
  case class UserContext(connP: TestProbe, user: User, proxyP: TestProbe) {
    lazy val proxy = UserProxy(user.id, proxyP.ref)
  }

  def withContext(testCode: Context => Any): Unit = withContextWithUsers()(testCode)

  def withContextWithUsers(users: User*)(testCode: Context => Any): Unit = {
    val ownerProbe = TestProbe()
    val conn = TestProbe()
    val rpa = system.actorOf(
      RoomProxyActor.props(room, UserProxy(owner.id, ownerProbe.ref)),
      s"room-proxy-actor-${now.toMillis}")

    val userMap: Map[String, UserContext] = users.zipWithIndex.map { case (user, index) =>
      val connP = TestProbe()
      val proxyP = TestProbe()
      val proxy = UserProxy(user.id, proxyP.ref)

      connP.ignoreMsg { case _ => true }

      connP.send(rpa, JoinRoom(proxy))
      proxyP.expectMsg(UserProxyActor.Subscribe(rpa))
      proxyP.send(rpa, UserProxyActor.Subscribed(rpa))
      proxyP.send(rpa, UserProxyActor.UserUpdated(user))

      (user.id, UserContext(connP, user, proxyP))
    }.toMap

    userMap.values.foreach(_.connP.ignoreNoMsg())

    testCode(Context(rpa, ownerProbe, conn, userMap))
  }
}
