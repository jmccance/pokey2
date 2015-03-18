package pokey.connection.model

import play.api.libs.json.Json
import pokey.connection.model.Commands._
import pokey.room.model.Estimate
import pokey.test.UnitSpec

class CommandSpecs extends UnitSpec {
  "A SetNameCommand" should {
    "deserialize from JSON correctly" in {
      val json =
        """
          |{
          |  "command": "setName",
          |  "name": "Cormen"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe SetNameCommand("Cormen")
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

      parseCommand(json).value shouldBe JoinRoomCommand("8675309")
    }
  }

  "A SubmitEstimateCommand" should {
    "deserialize correctly from JSON" in {
      val json =
        """
          |{
          |  "command": "submitEstimate",
          |  "roomId": "1234",
          |  "estimate": {
          |    "value": "XXS",
          |    "comment": "Easy-peasy"
          |  }
          |}
        """.stripMargin

      parseCommand(json).value shouldBe
        SubmitEstimateCommand("1234", Estimate(Some("XXS"), Some("Easy-peasy")))
    }
  }

  "A RevealRoomCommmand" should {
    "deserialize correctly from JSON" in {
      val json =
        """
          |{
          |  "command": "revealRoom",
          |  "roomId": "1234"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe RevealRoomCommand("1234")
    }
  }

  "A ClearRoomCommand" should {
    "deserialize correctly from JSON" in {
      val json =
        """
          |{
          |  "command": "clearRoom",
          |  "roomId": "1234"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe ClearRoomCommand("1234")
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

      parseCommand(json).value shouldBe an [InvalidCommand]
    }
  }

  private[this] def parseCommand(json: String): Option[Command] = Json.parse(json).asOpt[Command]
}
