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

  private[this] def EventObject(typeName: String)(fields: (String, Json.JsValueWrapper)*) =
    Json.obj(("event" -> (typeName: Json.JsValueWrapper)) +: fields: _*)

  case class UserUpdated(user: User) extends Event

  object UserUpdated {
    val writer = OWrites[UserUpdated] {
      case UserUpdated(user) =>
        EventObject("userUpdate")("response" -> "userUpdated", "user" -> user)
    }
  }

  case class RoomCreated(roomId: String) extends Event

  object RoomCreated {
    val writer = OWrites[RoomCreated] {
      case RoomCreated(roomId) => EventObject("roomCreated")("id" -> roomId)
    }
  }

  /**
   * Metadata for the given room. Used for things that either do not change or do not change often,
   * like the id, owner, estimate schema, etc., room name, etc.
   *
   * @param roomInfo the basic info for the room
   */
  case class RoomUpdated(roomInfo: RoomInfo) extends Event

  object RoomUpdated {
    val writer = OWrites[RoomUpdated] {
      case RoomUpdated(roomInfo) => EventObject("roomUpdate")("room" -> roomInfo)
    }
  }

  case class UserJoined(roomId: String, user: User) extends Event

  object UserJoined {
    val writer = OWrites[UserJoined] {
      case UserJoined(roomId, user) => EventObject("userJoined")("roomId" -> roomId, "user" -> user)
    }
  }

  case class UserLeft(roomId: String, user: User) extends Event

  object UserLeft {
    val writer = OWrites[UserLeft] {
      case UserLeft(roomId, user) => EventObject("userLeft")("roomId" -> roomId, "user" -> user)
    }
  }

  case class EstimateUpdated(roomId: String,
                             userId: String,
                             estimate: Option[PublicEstimate]) extends Event

  object EstimateUpdated {
    val writer = OWrites[EstimateUpdated] {
      case EstimateUpdated(roomId, userId, estimate) =>
        EventObject("estimateUpdated")(
          "roomId" -> roomId,
          "userId" -> userId,
          "estimate" -> estimate
        )
    }
  }

  case class RoomRevealed(roomId: String,
                          estimates: Map[String, Option[PublicEstimate]]) extends Event

  object RoomRevealed {
    val writer = OWrites[RoomRevealed] {
      case RoomRevealed(roomId, estimates) =>
        EventObject("roomRevealed")("roomId" -> roomId, "estimates" -> estimates)
    }
  }

  case class RoomCleared(roomId: String) extends Event

  object RoomCleared {
    val writer = OWrites[RoomCleared] {
      case RoomCleared(roomId) => EventObject("roomCleared")("roomId" -> roomId)
    }
  }

  case class RoomClosed(roomId: String) extends Event

  object RoomClosed {
    val writer = OWrites[RoomClosed] {
      case RoomClosed(roomId) => EventObject("roomClosed")("roomId" -> roomId)
    }
  }

  case class ErrorEvent(message: String) extends Event

  object ErrorEvent {
    val writer = OWrites[ErrorEvent] {
      case ErrorEvent(message) => EventObject("error")("message" -> message)
    }
  }
}
