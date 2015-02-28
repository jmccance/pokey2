package pokey.application.module

import pokey.assets.AssetController
import pokey.connection.ConnectionController
import scaldi._

class WebModule extends Module {
  binding to injected [AssetController]
  binding to injected [ConnectionController]
}
