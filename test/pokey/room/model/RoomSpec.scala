package pokey.room.model

import play.api.libs.json.{JsObject, Json}
import pokey.common.error.UnauthorizedErr
import pokey.test.UnitSpec
import pokey.user.model.User

class RoomSpec extends UnitSpec {

  "A Room" when {
    val emptyRoom = Room("room-id", "owner-id")
    val users = Seq(User("1", "John"), User("2", "George"), User("3", "Paul"), User("4", "Ringo"))
    val newUser = User("5", "Pete")
    val someRoom = emptyRoom ++ users
    val someEstimate = Estimate(Some("2"), Some("foo"))


    "a user is added" should {
      "add that user with an unfilled estimate" in {
        val updatedRoom = emptyRoom + newUser

        updatedRoom(newUser.id) shouldBe (newUser, None)
      }
    }

    "a user is removed" should {
      "remove that user from room" in {
        val user = users.head
        val updatedRoom = someRoom - user.id

        updatedRoom.users should not contain user
        updatedRoom.estimates should not contain key (user.id)
      }
    }

    "a user adds an estimate" should {
      "update the estimate if the user is in the room" in {
        val user = users.head
        val result = someRoom.withEstimate(user.id, someEstimate)
        result shouldBe 'good
        result.get(user.id) shouldBe (user, Some(someEstimate))
      }

      "return an UnauthorizedErr if the user is not in the room" in {
        val user = newUser
        val result = someRoom.withEstimate(user.id, someEstimate)
        result shouldBe 'bad
        result.swap.get shouldBe an [UnauthorizedErr]
      }
    }

    "serialized to JSON" should {
      "have the correct fields and values" in {
        val room = Room("1234", "abc")
        val json = Json.toJson(room.roomInfo).as[JsObject]

        (json \ "id").as[String] shouldBe "1234"
        (json \ "ownerId").as[String] shouldBe "abc"
        (json \ "isRevealed").as[Boolean] shouldBe false
      }
    }
  }
}
