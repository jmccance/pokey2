package pokey.room.model

import play.api.libs.json.{JsObject, Json}
import pokey.common.error.UnauthorizedErr
import pokey.test.UnitSpec
import pokey.user.model.User

class RoomSpec extends UnitSpec {

  "A Room" when {
    val users = Seq(User("1", "John"), User("2", "George"), User("3", "Paul"), User("4", "Ringo"))
    val estimates = Seq(
      Some(Estimate(Some("3"), None)),
      Some(Estimate(None, Some("No idea"))),
      None,
      None)
    val owner = users.head
    val notOwner = users.last
    val emptyRoom = Room("1234", owner.id)
    val newUser = User("5", "Pete")

    val someRoom =
      Room(
        "1234",
        owner.id,
        usersById = users.map(u => (u.id, u)).toMap,
        estimates = users.map(_.id).zip(estimates).toMap)

    val someEstimate = Estimate(Some("2"), Some("foo"))

    "queried whether it contains a user" should {
      "return true if it contains the user" in {
        someRoom.contains(users.head.id) shouldBe true
      }

      "return false if it does not contain the user" in {
        someRoom.contains(newUser.id) shouldBe false
      }
    }

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
        val result = someRoom.withEstimate(owner.id, someEstimate)
        result shouldBe 'good
        result.get(owner.id) shouldBe (owner, Some(someEstimate))
      }

      "return an UnauthorizedErr if the user is not in the room" in {
        val user = newUser
        val result = someRoom.withEstimate(user.id, someEstimate)
        result shouldBe 'bad
        result.swap.get shouldBe an [UnauthorizedErr]
      }
    }

    "a user reveals the room" should {
      "return the room toggled to revealed if the user owns the room" in {
        val result = someRoom.revealedBy(owner.id)
        result shouldBe 'good
        result.get shouldBe 'revealed
      }

      "return an UnauthorizedErr if the user is not the owner of the room" in {
        val result = someRoom.revealedBy(notOwner.id)
        result shouldBe 'bad
        result.swap.get shouldBe an [UnauthorizedErr]
      }
    }

    "it is not revealed" should {
      "return hidden public estimates" in {
        someRoom.publicEstimates.values should contain only (Some(HiddenEstimate), None)
      }
    }

    "it is revealed" should {
      "return revealed public estimates" in {
        val revealedRoom = someRoom.revealedBy(someRoom.ownerId).get
        forAll (revealedRoom.publicEstimates.values) {
          case Some(estimate) => estimate shouldBe a [RevealedEstimate]
          case None => /* None is fine. */
        }
      }
    }

    "a user clears the room" should {
      "return the room with estimates cleared and not revealed" in {
        val result = someRoom.clearedBy(owner.id)
        result shouldBe 'good
        result.get.estimates.values should contain only None
      }

      "return an UnauthorizedErr if the user is not the owner of the room" in {
        val result = someRoom.clearedBy(notOwner.id)
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
