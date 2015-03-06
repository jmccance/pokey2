package pokey.room.actor

import akka.actor._
import pokey.room.model.{Estimate, Room}
import pokey.user.actor.{UserProxy, UserProxyActor}
import pokey.user.model.User
import pokey.util.{Subscribable, TopicProtocol}

class RoomProxyActor(initialRoom: Room, ownerProxy: UserProxy)
  extends Actor
  with ActorLogging
  with Subscribable {
  import pokey.room.actor.RoomProxyActor._

  override protected val protocol: TopicProtocol = RoomProxyActor

  override def preStart(): Unit = context.watch(ownerProxy.actor)

  private[this] var room: Room = initialRoom

  def receive: Receive = handleSubscriptions orElse {
    case JoinRoom(userProxy) =>
      if (!room.contains(userProxy.id)) {
        // Subscribe to the user in order to get updates to names.
        userProxy.actor ! UserProxyActor.Subscribe(self)
        // Subscribe the connection to ourselves so they get updates
        self ! Subscribe(sender())
        // Add a stub user to the room.
        room += User(userProxy.id, "")
      }

    case user: User =>
      if (room.contains(user.id)) {
        room += user
        self ! Publish(RoomUpdated(room))
      }

    case LeaveRoom(userProxy) =>
      if (room.contains(userProxy.id)) {
        // Stop getting updates from the user
        userProxy.actor ! UserProxyActor.Unsubscribe(self)

        // Stop sending updates to the connection
        self ! Unsubscribe(sender())

        // Remove the user from the room.
        room -= userProxy.id

        // Update members of the change
        self ! Publish(RoomUpdated(room))
      }

    case SubmitEstimate(userId, estimate) =>
      room.withEstimate(userId, estimate).map { updatedRoom =>
        room = updatedRoom
        self ! Publish(RoomUpdated(room))
      } recover(sender ! _)

    case Reveal(userId: String) =>
      room.revealedBy(userId).map { updatedRoom =>
        room = updatedRoom
        self ! Publish(RoomUpdated(room))
      } recover(sender ! _)

    case Clear(userId: String) =>
      room.clearedBy(userId).map { updatedRoom =>
        room = updatedRoom
        self ! Publish(RoomUpdated(room))
      } recover(sender ! _)

    case Terminated(ownerProxy.actor) =>
      log.info("room_closed: {}, owner_id: {}", room.id, room.ownerId)
      self ! Publish(RoomClosed(room.id))
      context.stop(self)
  }
}

object RoomProxyActor extends TopicProtocol {
  def props(room: Room, ownerProxy: UserProxy) = Props(new RoomProxyActor(room, ownerProxy))

  /////////////
  // Commands

  case class JoinRoom(userProxy: UserProxy)

  case class LeaveRoom(userProxy: UserProxy)

  case class SubmitEstimate(userId: String, estimate: Estimate)

  case class Reveal(userId: String)

  case class Clear(userId: String)

  ///////////
  // Events

  case class RoomUpdated(room: Room)

  case class RoomClosed(roomId: String)
}
