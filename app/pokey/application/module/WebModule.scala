package pokey.application.module

import pokey.assets.AssetController
import pokey.connection.ConnectionController
import scaldi._

class WebModule extends Module {
  binding to new AssetController
  binding to new ConnectionController
}
