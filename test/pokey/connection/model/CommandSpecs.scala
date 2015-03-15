package pokey.connection.model

import play.api.libs.json.Json
import pokey.connection.model.Commands._
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
    "deserialize from JSON correctly when value and comment are present" in {
      val json =
        """
          |{
          |  "command": "submitEstimate",
          |  "roomId": "1234",
          |  "value": "XXS",
          |  "comment": "Easy-peasy"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe
        SubmitEstimateCommand("1234", Some("XXS"), Some("Easy-peasy"))
    }

    "deserialize from JSON correctly when only the value is present" in {
      val json =
        """
          |{
          |  "command": "submitEstimate",
          |  "roomId": "1234",
          |  "value": "XXS"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe SubmitEstimateCommand("1234", Some("XXS"), None)
    }

    "deserialize from JSON correctly when only the comment is present" in {
      val json =
        """
          |{
          |  "command": "submitEstimate",
          |  "roomId": "1234",
          |  "comment": "Not my job."
          |}
        """.stripMargin

      parseCommand(json).value shouldBe SubmitEstimateCommand("1234", None, Some("Not my job."))
    }

    "deserialize from JSON correctly when neither of the estimate fields are present" in {
      val json =
        """
          |{
          |  "command": "submitEstimate",
          |  "roomId": "1234"
          |}
        """.stripMargin

      parseCommand(json).value shouldBe SubmitEstimateCommand("1234", None, None)
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
