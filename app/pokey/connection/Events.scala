package pokey.connection

import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import pokey.room.Estimate
import pokey.user.User

sealed trait Event

object Event {
  import pokey.connection.Events._

  implicit val formatter = Format[Event](
    // Formatter is only use for messages sent out of the WebSocket handler, so no need to define
    // a reads.
    Reads.pure[Event](???),
    Writes[Event] {
      case r: UserUpdated => UserUpdated.writer.writes(r)
      case r: RoomCreated => RoomCreated.writer.writes(r)
      case r: RoomInfo => RoomInfo.writer.writes(r)
      case r: RoomState => RoomState.writer.writes(r)
      case r: ErrorEvent => ErrorEvent.writer.writes(r)
    }
  )

  implicit val responseFrameFormatter: FrameFormatter[Event] =
    FrameFormatter.jsonFrame[Event]
}

object Events {

  case class UserUpdated(user: User) extends Event

  object UserUpdated {
    val writer = Writes[UserUpdated] { resp =>
      Json.obj(
        "response" -> "userUpdated",
        "user" -> resp.user
      )
    }
  }

  case class RoomCreated(id: String) extends Event

  object RoomCreated {
    val writer = Writes[RoomCreated] { resp =>
      Json.obj(
        "response" -> "roomCreated",
        "id" -> resp.id
      )
    }
  }

  /**
   * Metadata for the given room. Used for things that either do not change or do not change often,
   * like the id, owner, estimate schema, etc., room name, etc.
   *
   * Sent when a connection joins a room or when the room info changes.
   *
   * @param id the id of the room
   * @param ownerId the id of the owner of the room
   */
  case class RoomInfo(id: String, ownerId: String)

  object RoomInfo {
    val writer = Writes[RoomInfo] { resp =>
      Json.obj(
        "response" -> "roomInfo",
        "id" -> resp.id,
        "ownerId" -> resp.ownerId
      )
    }
  }

  case class RoomState(id: String, isRevealed: Boolean, estimates: Map[User, Option[Estimate]])

  object RoomState {
    private[this] case object HiddenEstimate {
      implicit val writes = Writes[HiddenEstimate.type](_ => Json.obj())
    }

    val writer = Writes[RoomState] { resp =>
      val estimates = Json.toJson {
        // If estimates are revealed, we can just return the estimates map.
        // Otherwise we need to convert submitted estimates to hidden estimates, so that the client
        // can tell who estimated with knowing what they said.
        val screenedEstimates =
          if (resp.isRevealed) resp.estimates.mapValues(Json.toJson(_))
          else resp.estimates.mapValues(_.map(_ => HiddenEstimate)).mapValues(Json.toJson(_))

        // TODO This just associates a user id with an estimate. Will need to ensure the FE can
        // associate the id with a name.
        screenedEstimates.map {
          case (user, estimate) => user.id -> estimate
        }
      }

      Json.obj(
        "response" -> "roomState",
        "id" -> resp.id,
        "isRevealed" -> resp.isRevealed,
        "estimates" -> estimates
      )
    }
  }

  case class ErrorEvent(message: String) extends Event

  object ErrorEvent {
    val writer = Writes[ErrorEvent] { resp =>
      Json.obj(
        "response" -> "error",
        "message" -> resp.message
      )
    }
  }
}
