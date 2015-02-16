package pokey.application.module

import akka.actor.{ActorRef, ActorSystem}
import pokey.session.{DefaultSessionService, SessionRegistry, SessionService}
import scaldi.Module

import scala.concurrent.ExecutionContext

class DomainModule(implicit ec: ExecutionContext, system: ActorSystem) extends Module {
  bind [ActorSystem] to system
  bind [ExecutionContext] to ec
  bind [SessionService] to new DefaultSessionService

  bind [ActorRef] identifiedBy 'sessionRegistry to {
    implicit val system = inject [ActorSystem]

    system.actorOf(SessionRegistry.props)
  }
}
