package pokey.room.service

import pokey.room.actor.{RoomProxy, RoomRegistry}
import pokey.test.AkkaUnitSpec

class DefaultRoomServiceSpec extends AkkaUnitSpec {
  import system.dispatcher

  // The role of the room registry will be played by "self".

  "The DefaultRoomService" when {
    val ownerId = "1234"
    val roomId = "5678"
    val someActorRef = system.deadLetters

    "creating a room" should {
      "return a RoomProxy" in {
        val service = newRoomService()
        val fProxy = service.createRoom(ownerId)
        expectMsg(RoomRegistry.CreateRoomFor(ownerId))
        lastSender ! RoomProxy(roomId, someActorRef)

        whenReady(fProxy) { proxy =>
          proxy.id shouldBe roomId
          proxy.ref shouldBe someActorRef
        }
      }
    }

    "getting a room" should {
      "return the specified room if it exists" in {
        val service = newRoomService()
        val foProxy = service.getRoom(roomId)
        expectMsg(RoomRegistry.GetRoomProxy(roomId))
        lastSender ! Some(RoomProxy(roomId, someActorRef))

        whenReady(foProxy) { oProxy =>
          oProxy should not be empty
        }
      }

      "return None if the room does not exist" in {
        val service = newRoomService()
        val foProxy = service.getRoom(roomId)
        expectMsg(RoomRegistry.GetRoomProxy(roomId))
        lastSender ! None

        whenReady(foProxy) { oProxy =>
          oProxy shouldBe empty
        }
      }
    }

    def newRoomService(): RoomService = new DefaultRoomService(self)
  }
}
