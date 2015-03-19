package pokey.room.actor

import akka.actor._
import pokey.room.model.{Estimate, PublicEstimate, Room, RoomInfo}
import pokey.user.actor.{UserProxy, UserProxyActor}
import pokey.user.model.User
import pokey.util.{Subscribable, TopicProtocol}

class RoomProxyActor(initialRoom: Room, ownerProxy: UserProxy)
  extends Actor
  with ActorLogging
  with Subscribable {
  import pokey.room.actor.RoomProxyActor._

  override protected val protocol: TopicProtocol = RoomProxyActor

  context.watch(ownerProxy.ref)

  private[this] var room: Room = initialRoom

  override def onSubscribe(subscriber: ActorRef): Unit = {
    // Kind of hacky and noisy, but expedient. Simply send them "updates" for the current state of
    // the room.
    subscriber ! RoomUpdated(room.roomInfo)
    room.users.foreach(user => subscriber ! UserJoined(room.id, user))
    room.publicEstimates.foreach {
      case (userId, oEstimate) => EstimateUpdated(room.id, userId, oEstimate)
    }
  }

  override def onPublish(message: Any) = message match {
    case msg: Closed => context.stop(self)
    case _ => /* No action */
  }

  def receive: Receive = handleSubscriptions orElse {
    case JoinRoom(userProxy) =>
      // Subscribe to the user in order to get updates to names.
      userProxy.ref ! UserProxyActor.Subscribe(self)

      // Subscribe the connection to ourselves so they get updates
      self ! Subscribe(sender())

    case update @ UserProxyActor.UserUpdated(user) =>
      if (room.contains(user.id)) {
        log.info("room: {}, user_updated, user: {}", room.id, user)
        self ! Publish(update)
      } else {
        // We're assuming that we'll never be mistakenly sent an update message for a user who has
        // not first sent a JoinRoom message.
        self ! Publish(UserJoined(room.id, user))
        room += user
        log.info("room: {}, user_joined, user: {}", room.id, user)
      }

    case LeaveRoom(userProxy) =>
      if (room.contains(userProxy.id)) {
        // Stop getting updates from the user
        userProxy.ref ! UserProxyActor.Unsubscribe(self)

        // Stop sending updates to the connection
        self ! Unsubscribe(sender())

        // Remove the user from the room.
        val (user, _) = room(userProxy.id)
        room -= userProxy.id

        log.info("room: {}, user_left, user: {}", room.id, user)

        // Update members of the change
        self ! Publish(UserLeft(room.id, user))
      }

    case SubmitEstimate(userId, estimate) =>
      room.withEstimate(userId, estimate).map { updatedRoom =>
        room = updatedRoom
        self ! Publish(EstimateUpdated(room.id, userId, room.publicEstimates(userId)))
      } recover(sender ! _)

    case RevealFor(userId: String) =>
      room.revealedBy(userId).map { updatedRoom =>
        room = updatedRoom
        self ! Publish(Revealed(room.id, room.publicEstimates))
      } recover(sender ! _)

    case ClearFor(userId: String) =>
      room.clearedBy(userId).map { updatedRoom =>
        room = updatedRoom
        self ! Publish(Cleared(room.id))
      } recover(sender ! _)

    case Terminated(ownerProxy.ref) =>
      log.info("room_closed: {}, owner_id: {}", room.id, room.ownerId)
      self ! Publish(Closed(room.id))
      // Publishing a Closed message will terminate the RoomProxyActor.

    case _: UserProxyActor.Subscribed | _: UserProxyActor.Unsubscribed =>
      /* Expected, but not actionable */
  }
}

object RoomProxyActor extends TopicProtocol {
  def props(room: Room, ownerProxy: UserProxy) = Props(new RoomProxyActor(room, ownerProxy))

  /////////////
  // Commands
  
  sealed trait Command

  case class JoinRoom(userProxy: UserProxy) extends Command

  case class LeaveRoom(userProxy: UserProxy) extends Command

  case class SubmitEstimate(userId: String, estimate: Estimate) extends Command

  case class RevealFor(userId: String) extends Command

  case class ClearFor(userId: String) extends Command

  ///////////
  // Events

  sealed trait Event

  case class RoomUpdated(roomInfo: RoomInfo) extends Event

  case class UserJoined(roomId: String, user: User) extends Event

  case class UserLeft(roomId: String, user: User) extends Event

  case class EstimateUpdated(roomId: String,
                             userId: String,
                             estimate: Option[PublicEstimate]) extends Event

  case class Revealed(roomId: String, estimates: Map[String, Option[PublicEstimate]]) extends Event

  case class Cleared(roomId: String) extends Event

  case class Closed(roomId: String) extends Event
}
