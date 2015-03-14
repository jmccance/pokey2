package pokey.connection.model

import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import pokey.room.model.{PublicEstimate, RoomInfo}
import pokey.user.model.User

sealed trait Event

object Event {
  import Events._

  implicit val formatter = OFormat[Event](
    // Formatter is only use for messages sent out of the WebSocket handler, so no need to define
    // a reads.
    Reads.pure[Event](???),
    OWrites[Event] {
      case r: UserUpdated => UserUpdated.writer.writes(r)
      case r: RoomCreated => RoomCreated.writer.writes(r)
      case r: RoomUpdated => RoomUpdated.writer.writes(r)
      case r: UserJoined => UserJoined.writer.writes(r)
      case r: UserLeft => UserLeft.writer.writes(r)
      case r: EstimateUpdated => EstimateUpdated.writer.writes(r)
      case r: RoomRevealed => RoomRevealed.writer.writes(r)
      case r: RoomCleared => RoomCleared.writer.writes(r)
      case r: RoomClosed => RoomClosed.writer.writes(r)
      case r: ErrorEvent => ErrorEvent.writer.writes(r)
    }
  )

  implicit val responseFrameFormatter: FrameFormatter[Event] =
    FrameFormatter.jsonFrame[Event]
}

object Events {

  /**
   * The attached user has been updated in some way.
   *
   * @param user the updated user
   */
  case class UserUpdated(user: User) extends Event

  object UserUpdated {
    val writer = OWrites[UserUpdated] {
      case UserUpdated(user) =>
        EventJsObject("userUpdate")("response" -> "userUpdated", "user" -> user)
    }
  }

  /**
   * The client has successfully created a room.
   *
   * @param roomId the id of the newly created room
   */
  case class RoomCreated(roomId: String) extends Event

  object RoomCreated {
    val writer = OWrites[RoomCreated] {
      case RoomCreated(roomId) => EventJsObject("roomCreated")("roomId" -> roomId)
    }
  }

  /**
   * The attached room has been updated.
   *
   * @param roomInfo the metadata for the updated room
   */
  case class RoomUpdated(roomInfo: RoomInfo) extends Event

  object RoomUpdated {
    val writer = OWrites[RoomUpdated] {
      case RoomUpdated(roomInfo) => EventJsObject("roomUpdate")("room" -> roomInfo)
    }
  }

  /**
   * A user has joined the specified room.
   *
   * @param roomId the id of the room the user has joined
   * @param user the user that joined
   */
  case class UserJoined(roomId: String, user: User) extends Event

  object UserJoined {
    val writer = OWrites[UserJoined] {
      case UserJoined(roomId, user) => EventJsObject("userJoined")("roomId" -> roomId, "user" -> user)
    }
  }

  /**
   * A user has left the specified room.
   *
   * @param roomId the id of the room that the user has left
   * @param user the user that left
   */
  case class UserLeft(roomId: String, user: User) extends Event

  object UserLeft {
    val writer = OWrites[UserLeft] {
      case UserLeft(roomId, user) => EventJsObject("userLeft")("roomId" -> roomId, "user" -> user)
    }
  }

  /**
   * An estimate has been updated.
   *
   * @param roomId the room in which the estimate has been updated
   * @param userId the id of the user who updated their estimate
   * @param estimate the "public-facing" estimate, or None if they do not have an estimate
   */
  case class EstimateUpdated(roomId: String,
                             userId: String,
                             estimate: Option[PublicEstimate]) extends Event

  object EstimateUpdated {
    val writer = OWrites[EstimateUpdated] {
      case EstimateUpdated(roomId, userId, estimate) =>
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
  case class RoomRevealed(roomId: String,
                          estimates: Map[String, Option[PublicEstimate]]) extends Event

  object RoomRevealed {
    val writer = OWrites[RoomRevealed] {
      case RoomRevealed(roomId, estimates) =>
        EventJsObject("roomRevealed")("roomId" -> roomId, "estimates" -> estimates)
    }
  }

  /**
   * The specified room has had its estimates cleared. The client is responsible for zeroing out
   * all the estimates.
   *
   * @param roomId the id of the room that has been cleared
   */
  case class RoomCleared(roomId: String) extends Event

  object RoomCleared {
    val writer = OWrites[RoomCleared] {
      case RoomCleared(roomId) => EventJsObject("roomCleared")("roomId" -> roomId)
    }
  }

  /**
   * The given room has been closed and will no longer accept new members or estimate updates.
   *
   * @param roomId the id of the closed room
   */
  case class RoomClosed(roomId: String) extends Event

  object RoomClosed {
    val writer = OWrites[RoomClosed] {
      case RoomClosed(roomId) => EventJsObject("roomClosed")("roomId" -> roomId)
    }
  }

  /**
   * An error has occurred.
   *
   * @param message a message describing the nature of the error
   */
  case class ErrorEvent(message: String) extends Event

  object ErrorEvent {
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
