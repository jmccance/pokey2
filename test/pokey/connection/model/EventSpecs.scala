package pokey.connection.model

import play.api.libs.json.JsObject
import play.api.libs.json.Json._
import pokey.connection.model.Events._
import pokey.room.model.{RevealedEstimate, RoomInfo}
import pokey.test.UnitSpec
import pokey.user.model.User

import scala.util.{Success, Try}

class EventSpecs extends UnitSpec {

  private[this] val someUser = User("1234", "Phong")
  private[this] val someRoom = RoomInfo("5678", "1234", "Hot topic", isRevealed = false)
  private[this] val someEstimate = Some(RevealedEstimate(Some("XXS"), None))

  "A ConnectionInfoEvent event" should {
    "serialize to JSON correctly" in {
      val event = ConnectionInfo(someUser.id)
      writeEvent(event) shouldBe obj(
        "event" -> "connectionInfo",
        "userId" -> someUser.id
      )
    }
  }

  "A UserUpdatedEvent event" should {
    "serialize to JSON correctly" in {
      val event = UserUpdatedEvent(someUser)
      writeEvent(event) shouldBe obj(
        "event" -> "userUpdated",
        "user" -> someUser
      )
    }
  }

  "A RoomCreatedEvent event" should {
    "serialize to JSON correctly" in {
      val event = RoomCreatedEvent(someRoom.id)

      writeEvent(event) shouldBe obj(
        "event" -> "roomCreated",
        "roomId" -> someRoom.id
      )
    }
  }

  "A RoomUpdatedEvent event" should {
    "serialize to JSON correctly" in {
      val event = RoomUpdatedEvent(someRoom)

      writeEvent(event) shouldBe obj(
        "event" -> "roomUpdated",
        "room" -> someRoom
      )
    }
  }

  "A UserJoinedEvent event" should {
    "serialize to JSON correctly" in {
      val event = UserJoinedEvent(someRoom.id, someUser)

      writeEvent(event) shouldBe obj(
        "event" -> "userJoined",
        "roomId" -> someRoom.id,
        "user" -> someUser
      )
    }
  }

  "A UserLeftEvent event" should {
    "serialize to JSON correctly" in {
      val event = UserLeftEvent(someRoom.id, someUser)

      writeEvent(event) shouldBe obj(
        "event" -> "userLeft",
        "roomId" -> someRoom.id,
        "user" -> someUser
      )
    }
  }

  "A EstimateUpdatedEvent event" should {
    "serialize to JSON correctly" in {
      val event = EstimateUpdatedEvent(someRoom.id, someUser.id, someEstimate)

      writeEvent(event) shouldBe obj(
        "event" -> "estimateUpdated",
        "roomId" -> someRoom.id,
        "userId" -> someUser.id,
        "estimate" -> someEstimate
      )
    }
  }

  "A RoomRevealedEvent event" should {
    "serialize to JSON correctly" in {
      val estimates = Map(someUser.id -> someEstimate)
      val event = RoomRevealedEvent(someRoom.id, estimates)

      writeEvent(event) shouldBe obj(
        "event" -> "roomRevealed",
        "roomId" -> someRoom.id,
        "estimates" -> estimates
      )
    }
  }

  "A RoomClearedEvent event" should {
    "serialize to JSON correctly" in {
      val event = RoomClearedEvent(someRoom.id)

      writeEvent(event) shouldBe obj(
        "event" -> "roomCleared",
        "roomId" -> someRoom.id
      )
    }
  }

  "A RoomClosedEvent event" should {
    "serialize to JSON correctly" in {
      val event = RoomClosedEvent(someRoom.id)

      writeEvent(event) shouldBe obj(
        "event" -> "roomClosed",
        "roomId" -> someRoom.id
      )
    }
  }

  "A ErrorEvent event" should {
    "be creatable from a Throwable" in {
      val event = Try[Event] {
        throw new RuntimeException("Oops")
      }.recover(ErrorEvent.mapThrowable)

      event shouldBe an[Success[_]]
      event.get.asInstanceOf[ErrorEvent].message shouldBe "Oops"
    }

    "serialize to JSON correctly" in {
      val event = ErrorEvent("Oops")

      writeEvent(event) shouldBe obj(
        "event" -> "error",
        "message" -> event.message
      )
    }
  }

  "A HeartbeatEvent event" should {
    "serialize to JSON correctly" in {
      val event = HeartbeatEvent

      writeEvent(event) shouldBe obj(
        "event" -> "heartbeat"
      )
    }
  }

  def writeEvent(event: Event): JsObject = toJson(event).as[JsObject]
}
