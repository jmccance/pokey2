package pokey.connection.model

import play.api.libs.json._
import pokey.room.model.{PublicEstimate, Room, RoomInfo}
import pokey.user.model.User

sealed trait Event

object Event {
  import Events._

  implicit val writesEvent: Writes[Event] =
    OWrites[Event] {
      case r: ConnectionInfo => ConnectionInfo.writer.writes(r)
      case r: UserUpdatedEvent => UserUpdatedEvent.writer.writes(r)
      case r: RoomCreatedEvent => RoomCreatedEvent.writer.writes(r)
      case r: RoomUpdatedEvent => RoomUpdatedEvent.writer.writes(r)
      case r: UserJoinedEvent => UserJoinedEvent.writer.writes(r)
      case r: UserLeftEvent => UserLeftEvent.writer.writes(r)
      case r: EstimateUpdatedEvent => EstimateUpdatedEvent.writer.writes(r)
      case r: RoomRevealedEvent => RoomRevealedEvent.writer.writes(r)
      case r: RoomClearedEvent => RoomClearedEvent.writer.writes(r)
      case r: RoomClosedEvent => RoomClosedEvent.writer.writes(r)
      case HeartbeatEvent => HeartbeatEvent.writer.writes(HeartbeatEvent)
      case r: ErrorEvent => ErrorEvent.writer.writes(r)
    }
}

object Events {

  case class ConnectionInfo(userId: User.Id) extends Event

  object ConnectionInfo {
    val writer: OWrites[ConnectionInfo] = OWrites {
      case ConnectionInfo(user) =>
        EventJsObject("connectionInfo")("userId" -> user)
    }
  }

  /**
   * The attached user has been updated in some way.
   *
   * @param user the updated user
   */
  case class UserUpdatedEvent(user: User) extends Event

  object UserUpdatedEvent {
    val writer: OWrites[UserUpdatedEvent] = OWrites {
      case UserUpdatedEvent(user) =>
        EventJsObject("userUpdated")("user" -> user)
    }
  }

  /**
   * The client has successfully created a room.
   *
   * @param roomId the id of the newly created room
   */
  case class RoomCreatedEvent(roomId: Room.Id) extends Event

  object RoomCreatedEvent {
    val writer: OWrites[RoomCreatedEvent] = OWrites {
      case RoomCreatedEvent(roomId) => EventJsObject("roomCreated")("roomId" -> roomId)
    }
  }

  /**
   * The attached room has been updated.
   *
   * @param roomInfo the metadata for the updated room
   */
  case class RoomUpdatedEvent(roomInfo: RoomInfo) extends Event

  object RoomUpdatedEvent {
    val writer: OWrites[RoomUpdatedEvent] = OWrites {
      case RoomUpdatedEvent(roomInfo) => EventJsObject("roomUpdated")("room" -> roomInfo)
    }
  }

  /**
   * A user has joined the specified room.
   *
   * @param roomId the id of the room the user has joined
   * @param user the user that joined
   */
  case class UserJoinedEvent(roomId: Room.Id, user: User) extends Event

  object UserJoinedEvent {
    val writer: OWrites[UserJoinedEvent] = OWrites {
      case UserJoinedEvent(roomId, user) => EventJsObject("userJoined")("roomId" -> roomId, "user" -> user)
    }
  }

  /**
   * A user has left the specified room.
   *
   * @param roomId the id of the room that the user has left
   * @param user the user that left
   */
  case class UserLeftEvent(roomId: Room.Id, user: User) extends Event

  object UserLeftEvent {
    val writer: OWrites[UserLeftEvent] = OWrites {
      case UserLeftEvent(roomId, user) => EventJsObject("userLeft")("roomId" -> roomId, "user" -> user)
    }
  }

  /**
   * An estimate has been updated.
   *
   * @param roomId the room in which the estimate has been updated
   * @param userId the id of the user who updated their estimate
   * @param estimate the "public-facing" estimate, or None if they do not have an estimate
   */
  case class EstimateUpdatedEvent(
    roomId: Room.Id,
    userId: User.Id,
    estimate: Option[PublicEstimate]
  ) extends Event

  object EstimateUpdatedEvent {
    val writer: OWrites[EstimateUpdatedEvent] = OWrites {
      case EstimateUpdatedEvent(roomId, userId, estimate) =>
        EventJsObject("estimateUpdated")(
          "roomId" -> roomId,
          "userId" -> userId,
          "estimate" -> estimate
        )
    }
  }

  /**
   * The specified room as been revealed. Includes the revealed estimates.
   *
   * @param roomId the id of the room that has been revealed
   * @param estimates the revealed estimates for this room
   */
  case class RoomRevealedEvent(
    roomId: Room.Id,
    estimates: Map[User.Id, Option[PublicEstimate]]
  ) extends Event

  object RoomRevealedEvent {
    import play.api.libs.json.Json._

    val writer: OWrites[RoomRevealedEvent] = OWrites {
      case RoomRevealedEvent(roomId, estimates) =>
        EventJsObject("roomRevealed")(
          "roomId" -> roomId,
          "estimates" -> estimates
        )
    }
  }

  /**
   * The specified room has had its estimates cleared. The client is responsible for zeroing out
   * all the estimates.
   *
   * @param roomId the id of the room that has been cleared
   */
  case class RoomClearedEvent(roomId: Room.Id) extends Event

  object RoomClearedEvent {
    val writer: OWrites[RoomClearedEvent] = OWrites {
      case RoomClearedEvent(roomId) => EventJsObject("roomCleared")("roomId" -> roomId)
    }
  }

  /**
   * The given room has been closed and will no longer accept new members or estimate updates.
   *
   * @param roomId the id of the closed room
   */
  case class RoomClosedEvent(roomId: Room.Id) extends Event

  object RoomClosedEvent {
    val writer: OWrites[RoomClosedEvent] = OWrites {
      case RoomClosedEvent(roomId) => EventJsObject("roomClosed")("roomId" -> roomId)
    }
  }

  /**
   * Event sent to keep the connection alive.
   */
  case object HeartbeatEvent extends Event {
    val writer: OWrites[HeartbeatEvent.type] = OWrites { _ =>
      EventJsObject("heartbeat")()
    }
  }

  /**
   * An error has occurred.
   *
   * @param message a message describing the nature of the error
   */
  case class ErrorEvent(message: String) extends Event

  object ErrorEvent {
    val mapThrowable: PartialFunction[Throwable, ErrorEvent] = {
      case e => ErrorEvent(e.getMessage)
    }

    val writer: OWrites[ErrorEvent] = OWrites {
      case ErrorEvent(message) => EventJsObject("error")("message" -> message)
    }
  }

  /**
   * Helper for creating event JSON objects that conform to a consistent pattern.
   *
   * @param typeName the identifying label for this event
   * @param fields the fields to include in the event JSON
   * @return a JsObject that includes all the fields and the standardized identifying label field
   */
  private[this] def EventJsObject(typeName: String)(fields: (String, Json.JsValueWrapper)*) =
    Json.obj(("event" -> (typeName: Json.JsValueWrapper)) +: fields: _*)
}
