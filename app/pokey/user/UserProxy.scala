package pokey.user

import akka.actor._
import pokey.util.{Subscribable, TopicProtocol}

class UserProxy(user: User) extends Actor with ActorLogging with Subscribable {
  import pokey.user.UserProxy._

  protected val protocol = UserProxy

  private[this] case class State(user: User, connections: Set[ActorRef]) {
    def withUser(user: User) = this.copy(user = user)

    def withConnections(connections: Set[ActorRef]) = this.copy(connections = connections)
  }

  def withState(state: State): Receive = {
    val user = state.user
    val connections = state.connections

    handleSubscriptions orElse {
      case SetName(name) =>
        val updatedUser = user.copy(name = name)
        become(state.withUser(updatedUser))
        publish(UserUpdated(updatedUser))
        log.info("user_updated: {}", updatedUser)

      case NewConnection(conn) =>
        self ! Subscribe(conn)
        context.watch(conn)
        become(state.withConnections(connections + conn))
        log.info("new_connection: {}", conn)

      case Terminated(conn) if connections.contains(conn) =>
        val updatedConnections = connections - conn
        become(state.withConnections(updatedConnections))
        if (updatedConnections.isEmpty) {
          log.info("(NYI) user_cleanup_scheduled, user_id: {}", user.id)
          // TODO Schedule user cleanup after last connection is terminated
          // TODO Capture scheduled event so we can cancel if user reconnects before the timeout.
        }
    }
  }

  def receive = withState(State(user, Set.empty))

  private[this] def become(state: State) = context.become(withState(state))
}

object UserProxy extends TopicProtocol {
  def props(user: User) = Props(new UserProxy(user))

  case class NewConnection(conn: ActorRef)

  case class SetName(name: String)

  case class UserUpdated(user: User)
}
