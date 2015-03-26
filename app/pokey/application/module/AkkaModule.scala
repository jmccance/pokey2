package pokey.application.module

import akka.actor.ActorSystem
import scaldi.Module

class AkkaModule(system: ActorSystem) extends Module {
  bind[ActorSystem] to system
}
