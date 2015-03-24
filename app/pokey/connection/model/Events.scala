package pokey.connection.model

import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import pokey.room.model.{PublicEstimate, RoomInfo}
import pokey.user.model.User

sealed trait Event

object Event {
  import Events._

  implicit val formatter = OFormat[Event](
    // $COVERAGE-OFF$
    // Formatter is only use for messages sent out of the WebSocket handler, so no need to define
    // a reads.
    Reads.pure[Event](???),
    // $COVERAGE-ON$
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
      case r: ErrorEvent => ErrorEvent.writer.writes(r)
    }
  )

  implicit val responseFrameFormatter: FrameFormatter[Event] =
    FrameFormatter.jsonFrame[Event]
}

object Events {

  case class ConnectionInfo(userId: String) extends Event

  object ConnectionInfo {
    val writer = OWrites[ConnectionInfo] {
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
    val writer = OWrites[UserUpdatedEvent] {
      case UserUpdatedEvent(user) =>
        EventJsObject("userUpdated")("user" -> user)
    }
  }

  /**
   * The client has successfully created a room.
   *
   * @param roomId the id of the newly created room
   */
  case class RoomCreatedEvent(roomId: String) extends Event

  object RoomCreatedEvent {
    val writer = OWrites[RoomCreatedEvent] {
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
    val writer = OWrites[RoomUpdatedEvent] {
      case RoomUpdatedEvent(roomInfo) => EventJsObject("roomUpdated")("room" -> roomInfo)
    }
  }

  /**
   * A user has joined the specified room.
   *
   * @param roomId the id of the room the user has joined
   * @param user the user that joined
   */
  case class UserJoinedEvent(roomId: String, user: User) extends Event

  object UserJoinedEvent {
    val writer = OWrites[UserJoinedEvent] {
      case UserJoinedEvent(roomId, user) => EventJsObject("userJoined")("roomId" -> roomId, "user" -> user)
    }
  }

  /**
   * A user has left the specified room.
   *
   * @param roomId the id of the room that the user has left
   * @param user the user that left
   */
  case class UserLeftEvent(roomId: String, user: User) extends Event

  object UserLeftEvent {
    val writer = OWrites[UserLeftEvent] {
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
  case class EstimateUpdatedEvent(roomId: String,
                                  userId: String,
                                  estimate: Option[PublicEstimate]) extends Event

  object EstimateUpdatedEvent {
    val writer = OWrites[EstimateUpdatedEvent] {
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
  case class RoomRevealedEvent(roomId: String,
                               estimates: Map[String, Option[PublicEstimate]]) extends Event

  object RoomRevealedEvent {
    val writer = OWrites[RoomRevealedEvent] {
      case RoomRevealedEvent(roomId, estimates) =>
        EventJsObject("roomRevealed")("roomId" -> roomId, "estimates" -> estimates)
    }
  }

  /**
   * The specified room has had its estimates cleared. The client is responsible for zeroing out
   * all the estimates.
   *
   * @param roomId the id of the room that has been cleared
   */
  case class RoomClearedEvent(roomId: String) extends Event

  object RoomClearedEvent {
    val writer = OWrites[RoomClearedEvent] {
      case RoomClearedEvent(roomId) => EventJsObject("roomCleared")("roomId" -> roomId)
    }
  }

  /**
   * The given room has been closed and will no longer accept new members or estimate updates.
   *
   * @param roomId the id of the closed room
   */
  case class RoomClosedEvent(roomId: String) extends Event

  object RoomClosedEvent {
    val writer = OWrites[RoomClosedEvent] {
      case RoomClosedEvent(roomId) => EventJsObject("roomClosed")("roomId" -> roomId)
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

    val writer = OWrites[ErrorEvent] {
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
