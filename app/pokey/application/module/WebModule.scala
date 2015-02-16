package pokey.application.module

import pokey.assets.SessionAssetController
import pokey.websocket.SocketController
import scaldi._

class WebModule extends Module {
  binding to new SessionAssetController
  binding to new SocketController
}
