package pokey.connection.model

import play.api.libs.json.Json
import pokey.connection.model.Commands._
import pokey.room.model.Room.Id
import pokey.room.model.{Estimate, Room}
import pokey.test.UnitSpec
import pokey.user.model.User

class CommandSpecs extends UnitSpec {
  val roomId: Id = Room.Id.unsafeFrom("8675309")

  "A SetNameCommand" should {
    "deserialize from JSON correctly" in {
      val json =
        """
          |{
          |  "command": "setName",
          |  "name": "Cormen"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe SetNameCommand(User.Name.unsafeFrom("Cormen"))
    }
  }

  "A SetTopicCommand" should {
    "deserialize from JSON correctly" in {
      val json =
        """
          |{
          |  "command": "setTopic",
          |  "roomId": "8675309",
          |  "topic": "Hot Topic"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe SetTopicCommand(roomId, "Hot Topic")
    }
  }

  "A CreateRoomCommand" should {
    "deserialize from JSON correctly" in {
      val json =
        """
          |{
          |  "command": "createRoom"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe CreateRoomCommand
    }
  }

  "A JoinRoomCommand" should {
    "deserialize from JSON correctly" in {
      val json =
        """
          |{
          |  "command": "joinRoom",
          |  "roomId": "8675309"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe JoinRoomCommand(roomId)
    }
  }

  "A SubmitEstimateCommand" should {
    "deserialize correctly from JSON" in {
      val json =
        """
          |{
          |  "command": "submitEstimate",
          |  "roomId": "8675309",
          |  "estimate": {
          |    "value": "XXS",
          |    "comment": "Easy-peasy"
          |  }
          |}
        """.stripMargin

      parseCommand(json).value shouldBe
        SubmitEstimateCommand(roomId, Estimate(Some("XXS"), Some("Easy-peasy")))
    }
  }

  "A RevealRoomCommmand" should {
    "deserialize correctly from JSON" in {
      val json =
        """
          |{
          |  "command": "revealRoom",
          |  "roomId": "8675309"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe RevealRoomCommand(roomId)
    }
  }

  "A ClearRoomCommand" should {
    "deserialize correctly from JSON" in {
      val json =
        """
          |{
          |  "command": "clearRoom",
          |  "roomId": "8675309"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe ClearRoomCommand(roomId)
    }
  }

  "Command" should {
    "deserialize invalid JSON to an InvalidCommand" in {
      val json =
        """
          |{
          |  "foo": "bar"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe an[InvalidCommand]
    }
  }

  private[this] def parseCommand(json: String): Option[Command] = Json.parse(json).asOpt[Command]
}
