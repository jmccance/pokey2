package pokey.websocket

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter

sealed trait Request
sealed case class InvalidRequest(json: JsValue) extends Request

object InvalidRequest {
  implicit val reader: Reads[Request] = Reads.of[JsValue].map(InvalidRequest(_))
}

object Request {
  import pokey.websocket.Requests._

  implicit val formatter = Format[Request](
    SetName.reader
      orElse CreateRoom.reader
      orElse JoinRoom.reader
      orElse Estimate.reader
      orElse Reveal.reader
      orElse Hide.reader
      orElse Clear.reader
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
    val hide = "hide"
    val clear = "clear"
  }

  case class SetName(name: String) extends Request

  object SetName {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.setName)
        andKeep (JsPath \ "name").read[String].map(SetName(_)))
  }

  case object CreateRoom extends Request {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.createRoom)
        andKeep Reads.pure(CreateRoom))
  }

  case class JoinRoom(roomId: String) extends Request

  object JoinRoom {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.joinRoom)
        andKeep (JsPath \ "roomId").read[String]).map(JoinRoom(_))
  }

  case class Estimate(roomId: String, value: String, comment: String) extends Request

  object Estimate {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.estimate)
        andKeep (JsPath \ "roomId").read[String]
        and (JsPath \ "value").read[String]
        and (JsPath \ "comment").read[String])(Estimate.apply _)
  }
  
  case class Reveal(roomId: String) extends Request
  
  object Reveal {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.reveal)
        andKeep (JsPath \ "roomId").read[String]).map(Reveal(_))
  }
  
  case class Hide(roomId: String) extends Request
  
  object Hide {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.hide)
        andKeep (JsPath \ "roomId").read[String]).map(Hide(_))
  }
  
  case class Clear(roomId: String) extends Request
  
  object Clear {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.clear)
        andKeep (JsPath \ "roomId").read[String]).map(Clear(_))
  }
}
