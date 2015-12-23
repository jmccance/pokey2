package pokey.room.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import pokey.common.error.UnauthorizedErr
import pokey.room.actor.RoomProxyActor._
import pokey.room.model.{ Estimate, Room }
import pokey.test.AkkaUnitSpec
import pokey.user.actor.{ UserProxy, UserProxyActor }
import pokey.user.model.User

class RoomProxyActorSpec extends AkkaUnitSpec {
  val owner = User("U-1", "Esme")
  val someUser = User("U-2", "Magrat")
  val someEstimate = Estimate(Some("2"), None)
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
      "publish the update to the room" in withContextWithUsers(someUser, anotherUser) { ctx =>
        val connP = ctx.users(anotherUser.id).connP
        val updatedUser = someUser.copy(name = "Verence")
        ctx.users(someUser.id).proxyP.send(ctx.rpa, UserProxyActor.UserUpdated(updatedUser))
        connP.fishForMessage() {
          case UserProxyActor.UserUpdated(`updatedUser`) => true
          case _ => false
        }
      }

      "publish the updated version to new users that join" in
        withContextWithUsers(someUser) {
          case Context(rpa, ownerProbe, connProbe, _) =>

            // "someUser" changes their name.
            val someUserProxyProbe = TestProbe()
            val updatedSomeUser = someUser.copy(name = s"${someUser.name} II")
            someUserProxyProbe.send(rpa, UserProxyActor.UserUpdated(updatedSomeUser))

            // "anotherUser" joins
            val anotherUserConnProbe = TestProbe()
            val anotherUserProxy = UserProxy(anotherUser.id, TestProbe().ref)

            anotherUserConnProbe.send(rpa, JoinRoom(anotherUserProxy))

            // "anotherUser" should see the new name.
            anotherUserConnProbe.fishForMessage(hint = "Did not receive UserJoined for updated user") {
              case UserJoined(`roomId`, User(someUser.id, name)) =>
                name shouldBe updatedSomeUser.name
                true

              case msg => false
            }
        }
    }

    "it receives a LeaveRoom message" which {
      "has a UserProxy that is a member" should {
        "unsubscribe from the UserProxy and publish a UserLeft message" in
          withContextWithUsers(someUser, anotherUser) { ctx =>
            val someUserCtx = ctx.users(someUser.id)
            someUserCtx.connP.ignoreNoMsg()
            val anotherUserCtx = ctx.users(anotherUser.id)
            anotherUserCtx.connP.send(ctx.rpa, RoomProxyActor.LeaveRoom(anotherUserCtx.proxy))
            anotherUserCtx.proxyP.expectMsg(UserProxyActor.Unsubscribe(ctx.rpa))

            someUserCtx.connP.expectMsgEventually(UserLeft(roomId, anotherUserCtx.user))
          }
      }
    }

    "it receives a SubmitEstimate message from a connection" which {
      "is joined to the room" should {
        "send an EstimateUpdated out to members of the room" in
          withContextWithUsers(someUser, anotherUser) { ctx =>
            val someUserCtx = ctx.users(someUser.id)
            someUserCtx.connP.send(ctx.rpa, SubmitEstimate(someUser.id, someEstimate))

            val anotherUserCtx = ctx.users(anotherUser.id)
            anotherUserCtx.connP.expectMsgEventually {
              EstimateUpdated(roomId, someUser.id, Option(someEstimate.asHidden))
            }
          }
      }

      "is not joined to the room" should {
        "reply with an error" in withContext { ctx =>
          val unauthorizedConnP = TestProbe()
          unauthorizedConnP.send(ctx.rpa, SubmitEstimate(someUser.id, someEstimate))

          unauthorizedConnP.expectMsgType[UnauthorizedErr]
        }
      }
    }

    "it receives a RevealFor message for a userId" which {
      "is allowed to reveal the room" should {
        "send a Revealed message to the room" in withContextWithUsers(owner, someUser) { ctx =>
          val ownerCtx = ctx.users(owner.id)
          ownerCtx.connP.send(ctx.rpa, RevealFor(owner.id))

          ctx.users(someUser.id).connP.fishForMessage() {
            case _: Revealed => true
            case _ => false
          }
        }
      }

      "is not allowed to reveal the room" should {
        "forward the error back to the connection" in withContextWithUsers(someUser) { ctx =>
          val someUserCtx = ctx.users(someUser.id)
          someUserCtx.connP.send(ctx.rpa, RevealFor(someUser.id))

          someUserCtx.connP.fishForMessage() {
            case _: UnauthorizedErr => true
            case _ => false
          }
        }
      }
    }

    "it receives a ClearFor message for a userId" which {
      "is allowed to Clear the room" should {
        "send a Cleared message to the room" in withContextWithUsers(owner, someUser) { ctx =>
          val ownerCtx = ctx.users(owner.id)
          ownerCtx.connP.send(ctx.rpa, ClearFor(owner.id))

          ctx.users(someUser.id).connP.expectMsgEventually(Cleared(roomId))
        }
      }

      "is not allowed to Clear the room" should {
        "forward the error back to the connection" in withContextWithUsers(someUser) { ctx =>
          val someUserCtx = ctx.users(someUser.id)
          someUserCtx.connP.send(ctx.rpa, ClearFor(someUser.id))

          someUserCtx.connP.fishForMessage() {
            case _: UnauthorizedErr => true
            case _ => false
          }
        }

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

    val userMap: Map[String, UserContext] = users.zipWithIndex.map {
      case (user, index) =>
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
