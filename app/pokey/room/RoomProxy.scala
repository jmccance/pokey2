package pokey.room

import akka.actor._
import pokey.util.{Subscribable, TopicProtocol}

class RoomProxy(room: Room, ownerProxy: ActorRef)
  extends Actor
  with ActorLogging
  with Subscribable {
  import pokey.room.RoomProxy._

  override protected val protocol: TopicProtocol = RoomProxy

  override def preStart(): Unit = context.watch(ownerProxy)

  override def receive: Receive = handleSubscriptions orElse {
    case JoinRoom(userId: String) =>
      // Validate user is not already a member. No-op if so.
      // Subscribe the sender.
      // Add the user to the room.
      // Publish updated RoomInfo and RoomState events.

    case LeaveRoom(userId) =>
      // Validate the user is a member. No-op if not.
      // Unsubscribe the sender
      // Remove the user from the room.
      // Publish updated RoomInfo and RoomState events.

    case SubmitEstimate(userId, estimate) =>
      // Validate user is a member. Otherwise reply with an error.
      // Update the room.
      // Publish a RoomState event.

    case Reveal(userId: String) =>
      // Validate user is owner. Otherwise reply with an error.
      // Update the room.
      // Publish a RoomState event.

    case Clear(userId: String) =>
      // Validate user is owner. Otherwise reply with an error.
      // Update the room.
      // Publish a RoomState event.

    case Terminated(`ownerProxy`) =>
      log.info("room_closed: {}, owner_id: {}", room.id, room.ownerId)
      publish(RoomClosed(room.id))
      context.stop(self)
  }
}

object RoomProxy extends TopicProtocol {
  def props(room: Room, ownerProxy: ActorRef) = Props(new RoomProxy(room, ownerProxy))

  case class JoinRoom(userId: String)

  case class LeaveRoom(userId: String)

  case class SubmitEstimate(userId: String, estimate: Estimate)

  case class Reveal(userId: String)

  case class Clear(userId: String)

  case class RoomClosed(roomId: String)
}
