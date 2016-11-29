package pokey.room.model

import org.scalactic.{Bad, Good, Or}
import play.api.libs.json._
import pokey.common.error.UnauthorizedErr
import pokey.user.model.User
import play.api.libs.functional.syntax._

case class Room(
    id: Room.Id,
    ownerId: User.Id,
    topic: String,
    isRevealed: Boolean = false,
    private val usersById: Map[User.Id, User] = Map.empty,
    estimates: Map[User.Id, Option[Estimate]] = Map.empty
) {
  lazy val users = usersById.values
  lazy val roomInfo: RoomInfo = RoomInfo(id, ownerId, topic, isRevealed)

  lazy val publicEstimates: Map[User.Id, Option[PublicEstimate]] =
    if (isRevealed) estimates.mapValues(_.map(_.asRevealed))
    else estimates.mapValues(_.map(_.asHidden))

  def apply(userId: User.Id): (User, Option[Estimate]) = this.get(userId).get

  def get(userId: User.Id): Option[(User, Option[Estimate])] = {
    val oUser = usersById.get(userId)
    oUser.map(user => (user, estimates(user.id)))
  }

  def +(user: User): Room = {
    val mUsers = usersById + (user.id -> user)

    val mEstimates =
      if (usersById.contains(user.id)) estimates
      else estimates + (user.id -> None)

    this.copy(usersById = mUsers, estimates = mEstimates)
  }

  def -(userId: User.Id): Room = {
    val mUsers = usersById - userId
    val mEstimates = estimates - userId
    this.copy(usersById = mUsers, estimates = mEstimates)
  }

  def contains(userId: User.Id): Boolean = usersById.contains(userId)

  def withEstimate(userId: User.Id, estimate: Estimate): Room Or UnauthorizedErr =
    if (estimates.contains(userId)) {
      val updatedEstimates = estimates + (userId -> Some(estimate))
      Good(this.copy(estimates = updatedEstimates))
    } else {
      Bad(UnauthorizedErr("Only a member of a room may submit an estimate to it."))
    }

  def clearedBy(userId: User.Id): Room Or UnauthorizedErr =
    if (userId == ownerId) {
      val clearedEstimates = estimates.mapValues(_ => None)
      Good(this.copy(estimates = clearedEstimates, isRevealed = false))
    } else {
      Bad(UnauthorizedErr("Only the room owner may clear the room's estimates."))
    }

  def revealedBy(userId: User.Id): Room Or UnauthorizedErr =
    if (userId == ownerId) {
      Good(this.copy(isRevealed = true))
    } else {
      Bad(UnauthorizedErr("Only the room owner may reveal the room's estimates."))
    }

  def topicSetBy(userId: User.Id, topic: String): Room Or UnauthorizedErr =
    if (userId == ownerId) {
      Good(this.copy(topic = topic))
    } else {
      Bad(UnauthorizedErr("Only the room owner may set the room topic."))
    }
}

object Room {
  abstract case class Id private (value: String)

  object Id {
    def from(s: String): Option[Id] = Option(s).filter(_.nonEmpty).map(new Id(_) {})

    def unsafeFrom(s: String): Id = new Id(s) {}

    implicit val writesId: Writes[Id] = Writes.of[String].contramap(_.value)

    implicit val readsId: Reads[Id] = Reads.of[String].map(Id.from).flatMap {
      case Some(name) => Reads.pure(name)
      case None => Reads(_ => JsError("error.user.name.empty"))
    }
  }
}

case class RoomInfo(id: Room.Id, ownerId: User.Id, topic: String, isRevealed: Boolean)

object RoomInfo {
  implicit val writer: OWrites[RoomInfo] = OWrites[RoomInfo] {
    case RoomInfo(id, ownerId, topic, isRevealed) =>
      Json.obj(
        "id" -> id,
        "ownerId" -> ownerId,
        "topic" -> topic,
        "isRevealed" -> isRevealed
      )
  }
}
