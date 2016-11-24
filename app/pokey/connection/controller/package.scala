package pokey.connection

import play.api.mvc.WebSocket.MessageFlowTransformer
import pokey.connection.model.{Command, Event}

package object controller {
  implicit val connectionMessageFlowTransformer: MessageFlowTransformer[Command, Event] =
    MessageFlowTransformer.jsonMessageFlowTransformer[Command, Event]
}
