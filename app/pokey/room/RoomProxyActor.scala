package pokey.room

import akka.actor._
import pokey.user.{UserProxy, UserProxyActor}
import pokey.util.{Subscribable, TopicProtocol}

class RoomProxyActor(initialRoom: Room, ownerProxy: UserProxy)
  extends Actor
  with ActorLogging
  with Subscribable {
  import pokey.room.RoomProxyActor._

  override protected val protocol: TopicProtocol = RoomProxyActor

  override def preStart(): Unit = context.watch(ownerProxy.actor)

  override def receive: Receive = withRoom(initialRoom)

  private[this] def withRoom(room: Room): Receive = handleSubscriptions orElse {
    case JoinRoom(userProxy) =>
      if (!room.contains(userProxy.id)) {
        // Get the current user state via an ask.
        // Subscribe the user in order to get updates to names.
        userProxy.actor ! UserProxyActor.Subscribe(self)
        // Add the user to the room.
        // Publish updated RoomInfo and RoomState events.
      }

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

    case Terminated(ownerProxy.actor) =>
      log.info("room_closed: {}, owner_id: {}", room.id, room.ownerId)
      publish(RoomClosed(room.id))
      context.stop(self)
  }

  private[this] def become(room: Room) = context.become(withRoom(room))
}

object RoomProxyActor extends TopicProtocol {
  def props(room: Room, ownerProxy: UserProxy) = Props(new RoomProxyActor(room, ownerProxy))

  case class JoinRoom(userProxy: UserProxy)

  case class LeaveRoom(userId: String)

  case class SubmitEstimate(userId: String, estimate: Estimate)

  case class Reveal(userId: String)

  case class Clear(userId: String)

  case class RoomClosed(roomId: String)
}
