package pokey.connection.model

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter

sealed trait Command
sealed case class InvalidCommand(json: JsValue) extends Command

object InvalidCommand {
  implicit val reader: Reads[Command] = Reads.of[JsValue].map(InvalidCommand(_))
}

object Command {
  import Commands._

  implicit val formatter = Format[Command](
    SetNameCommand.reader
      orElse CreateRoomCommand.reader
      orElse JoinRoomCommand.reader
      orElse SubmitEstimateCommand.reader
      orElse RevealRoomCommand.reader
      orElse ClearRoomCommand.reader
      orElse InvalidCommand.reader,
    // We never write this, so skipping implementation.
    // $COVERAGE-OFF$
    Writes[Command](_ => ???)
    // $COVERAGE-ON$
  )

  implicit val frameFormatter: FrameFormatter[Command] = FrameFormatter.jsonFrame[Command]
}

trait CommandCompanion {
  val jsonId: String

  protected def validateType: Reads[JsValue] =
    (JsPath \ "command").read[String].filter(_ == jsonId) andKeep Reads.of[JsValue]
}

object Commands {
  case class SetNameCommand(name: String) extends Command

  object SetNameCommand extends CommandCompanion {
    val jsonId = "setName"

    val reader: Reads[Command] =
      validateType andKeep (JsPath \ "name").read[String].map(SetNameCommand(_))
  }

  case object CreateRoomCommand extends Command with CommandCompanion {
    val jsonId = "createRoom"

    val reader: Reads[Command] = validateType andKeep Reads.pure(CreateRoomCommand)
  }

  case class JoinRoomCommand(roomId: String) extends Command

  object JoinRoomCommand extends CommandCompanion {
    val jsonId = "joinRoom"

    val reader: Reads[Command] =
      validateType andKeep (JsPath \ "roomId").read[String].map(JoinRoomCommand(_))
  }

  case class SubmitEstimateCommand(roomId: String,
                                   value: Option[String],
                                   comment: Option[String]) extends Command

  object SubmitEstimateCommand extends CommandCompanion {
    val jsonId = "submitEstimate"

    val reader: Reads[Command] =
      validateType andKeep
        ((JsPath \ "roomId").read[String]
          and (JsPath \ "value").readNullable[String]
          and (JsPath \ "comment").readNullable[String])(SubmitEstimateCommand.apply _)
  }

  case class RevealRoomCommand(roomId: String) extends Command

  object RevealRoomCommand extends CommandCompanion {
    val jsonId = "revealRoom"

    val reader: Reads[Command] =
      validateType andKeep (JsPath \ "roomId").read[String].map(RevealRoomCommand(_))
  }

  case class ClearRoomCommand(roomId: String) extends Command

  object ClearRoomCommand extends CommandCompanion {
    val jsonId = "clearRoom"

    val reader: Reads[Command] =
      validateType andKeep (JsPath \ "roomId").read[String].map(ClearRoomCommand(_))
  }
}
