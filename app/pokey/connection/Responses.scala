package pokey.connection

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter

sealed trait Response

object Response {
  import pokey.connection.Responses._

  implicit val formatter = new Format[Response] {
    override def reads(json: JsValue): JsResult[Response] = ???

    override def writes(resp: Response): JsValue = resp match {
      case resp: ErrorResponse => ErrorResponse.writer.writes(resp)
    }
  }

  implicit val responseFrameFormatter: FrameFormatter[Response] =
    FrameFormatter.jsonFrame[Response]
}

object Responses {
  case class ErrorResponse(message: String) extends Response

  object ErrorResponse {
    implicit val writer = (JsPath \ "message").write[String].contramap { resp: ErrorResponse =>
      resp.message
    }
  }
}
