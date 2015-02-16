package pokey.websocket

import play.api.libs.json._

sealed trait Request
sealed case class InvalidRequest(json: JsValue) extends Request

object Request {
  implicit val formatter = new Format[Request] {
    import pokey.websocket.Requests._

    override def reads(json: JsValue): JsResult[Request] = {
      val messageType = for {
        jsObject <- json.asOpt[JsObject]
        messageValue <- jsObject.value.get("request")
        message <- messageValue.asOpt[String]
      } yield message

      messageType.map {
        case RequestType.setName => SetName.reader.reads(json)
      }.getOrElse(JsSuccess(InvalidRequest(json)))
    }

    override def writes(req: Request): JsValue = ???
  }
}

object Requests {
  object RequestType {
    val setName = "setName"
  }

  case class SetName(name: String) extends Request

  object SetName {
    implicit val reader: Reads[SetName] = (JsPath \ "name").read[String].map(SetName(_))
  }
}
