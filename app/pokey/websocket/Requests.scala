package pokey.websocket

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait Request
sealed case class InvalidRequest(json: JsValue) extends Request

object InvalidRequest {
  implicit val reader: Reads[Request] = Reads.of[JsValue].map(InvalidRequest(_))
}

object Request {
  import pokey.websocket.Requests._

  implicit val formatter = Format[Request](
    SetName.reader
      orElse JoinRoom.reader
      orElse InvalidRequest.reader,
    Writes[Request](_ => ???)
  )
}

object Requests {
  object RequestType {
    val setName = "setName"
    val joinRoom = "joinRoom"
  }

  case class SetName(name: String) extends Request

  object SetName {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.setName) andKeep
        (JsPath \ "name").read[String]).map(SetName(_))
  }

  case class JoinRoom(id: String) extends Request

  object JoinRoom {
    val reader: Reads[Request] =
      ((JsPath \ "request").read[String].filter(_ == RequestType.joinRoom)
        andKeep (JsPath \ "id").read[String]).map(JoinRoom(_))
  }

  case object CreateRoom
}
