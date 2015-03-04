package pokey.room

import akka.actor._
import pokey.user.{User, UserProxy, UserProxyActor}
import pokey.util.{Subscribable, TopicProtocol}

class RoomProxyActor(initialRoom: Room, ownerProxy: UserProxy)
  extends Actor
  with ActorLogging
  with Subscribable {
  import pokey.room.RoomProxyActor._

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
      } recover {
        case e => // TODO Reply with error
      }

    case Reveal(userId: String) =>
      if (room.ownerId == userId) {
        if (!room.isRevealed) {
          room = room.revealed()
          self ! Publish(RoomUpdated(room))
        }
      } else {
        // TODO Reply with error about room ownership
        // Should change method to move validation of ownership into the model. See withEstimate.
      }

    case Clear(userId: String) =>
      if (room.ownerId == userId) {
        room = room.cleared()
        self ! Publish(RoomUpdated(room))
      } else {
        // TODO Reply with error about room ownership
        // Should change method to move validation of ownership into the model
      }

    case Terminated(ownerProxy.actor) =>
      log.info("room_closed: {}, owner_id: {}", room.id, room.ownerId)
      self ! Publish(RoomClosed(room.id))
      context.stop(self)
  }
}

object RoomProxyActor extends TopicProtocol {
  def props(room: Room, ownerProxy: UserProxy) = Props(new RoomProxyActor(room, ownerProxy))

  case class JoinRoom(userProxy: UserProxy)

  case class LeaveRoom(userProxy: UserProxy)

  case class SubmitEstimate(userId: String, estimate: Estimate)

  case class Reveal(userId: String)

  case class Clear(userId: String)

  case class RoomClosed(roomId: String)

  case class RoomUpdated(room: Room)
}
