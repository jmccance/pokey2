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

object Requests {
  object RequestType {
    val setName = "setName"
    val createRoom = "createRoom"
    val joinRoom = "joinRoom"
    val estimate = "estimate"
    val reveal = "reveal"
    val clear = "clear"
  }

  case class SetNameRequest(name: String) extends Request

  object SetNameRequest {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.setName)
        andKeep (JsPath \ "name").read[String].map(SetNameRequest(_)))
  }

  case object CreateRoomRequest extends Request {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.createRoom)
        andKeep Reads.pure(CreateRoomRequest))
  }

  case class JoinRoomRequest(roomId: String) extends Request

  object JoinRoomRequest {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.joinRoom)
        andKeep (JsPath \ "roomId").read[String]).map(JoinRoomRequest(_))
  }

  case class SubmitEstimateRequest(roomId: String,
                                   value: Option[String],
                                   comment: Option[String]) extends Request

  object SubmitEstimateRequest {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.estimate)
        andKeep (JsPath \ "roomId").read[String]
        and (JsPath \ "value").readNullable[String]
        and (JsPath \ "comment").readNullable[String])(SubmitEstimateRequest.apply _)
  }

  case class RevealRoomRequest(roomId: String) extends Request

  object RevealRoomRequest {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.reveal)
        andKeep (JsPath \ "roomId").read[String]).map(RevealRoomRequest(_))
  }

  case class ClearRoomRequest(roomId: String) extends Request

  object ClearRoomRequest {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.clear)
        andKeep (JsPath \ "roomId").read[String]).map(ClearRoomRequest(_))
  }
}
