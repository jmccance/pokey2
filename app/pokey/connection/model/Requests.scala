package pokey.connection.model

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter

sealed trait Request
sealed case class InvalidRequest(json: JsValue) extends Request

object InvalidRequest {
  implicit val reader: Reads[Request] = Reads.of[JsValue].map(InvalidRequest(_))
}

object Request {
  import Requests._

  implicit val formatter = Format[Request](
    SetNameRequest.reader
      orElse CreateRoomRequest.reader
      orElse JoinRoomRequest.reader
      orElse SubmitEstimateRequest.reader
      orElse RevealRoomRequest.reader
      orElse ClearRoomRequest.reader
      orElse InvalidRequest.reader,
    Writes[Request](_ => ???)
  )

  implicit val requestFrameFormatter: FrameFormatter[Request] =
    FrameFormatter.jsonFrame[Request]
}

trait RequestCompanion {
  val jsonId: String

  private[model] def validateType: Reads[JsValue] =
    (JsPath \ "request").read[String].filter(_ == jsonId) andKeep Reads.of[JsValue]
}

object Requests {
  case class SetNameRequest(name: String) extends Request

  object SetNameRequest extends RequestCompanion {
    val jsonId = "setName"

    val reader: Reads[Request] =
      validateType andKeep (JsPath \ "name").read[String].map(SetNameRequest(_))
  }

  case object CreateRoomRequest extends Request with RequestCompanion {
    val jsonId = "createRoom"

    val reader: Reads[Request] = validateType andKeep Reads.pure(CreateRoomRequest)
  }

  case class JoinRoomRequest(roomId: String) extends Request

  object JoinRoomRequest extends RequestCompanion {
    val jsonId = "joinRoom"

    val reader: Reads[Request] =
      validateType andKeep (JsPath \ "roomId").read[String].map(JoinRoomRequest(_))
  }

  case class SubmitEstimateRequest(roomId: String,
                                   value: Option[String],
                                   comment: Option[String]) extends Request

  object SubmitEstimateRequest extends RequestCompanion {
    val jsonId = "submitEstimate"

    val reader: Reads[Request] =
      validateType andKeep
        ((JsPath \ "roomId").read[String]
          and (JsPath \ "value").readNullable[String]
          and (JsPath \ "comment").readNullable[String])(SubmitEstimateRequest.apply _)
  }

  case class RevealRoomRequest(roomId: String) extends Request

  object RevealRoomRequest extends RequestCompanion {
    val jsonId = "revealRoom"

    val reader: Reads[Request] =
      validateType andKeep (JsPath \ "roomId").read[String].map(RevealRoomRequest(_))
  }

  case class ClearRoomRequest(roomId: String) extends Request

  object ClearRoomRequest extends RequestCompanion {
    val jsonId = "clearRoom"

    val reader: Reads[Request] =
      validateType andKeep (JsPath \ "roomId").read[String].map(ClearRoomRequest(_))
  }
}
