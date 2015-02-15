package pokey.room

import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current

object RoomController extends Controller {
  def room = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    RoomWSHandler.props(out)
  }
}
