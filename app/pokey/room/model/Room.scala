package pokey.room.model

import org.scalactic.{Bad, Good, Or}
import play.api.libs.json.{Json, OWrites}
import pokey.common.error.UnauthorizedErr
import pokey.user.model.User

case class Room(id: String,
                ownerId: String,
                isRevealed: Boolean = false,
                users: Set[User] = Set.empty,
                estimates: Map[String, Option[Estimate]] = Map.empty) {
  def roomInfo: RoomInfo = RoomInfo(id, ownerId, isRevealed)

  lazy val publicEstimates: Map[String, Option[PublicEstimate]] =
    if (isRevealed) estimates.mapValues(_.map(_.asRevealed))
    else estimates.mapValues(_.map(_.asHidden))

  def apply(userId: String): (User, Option[Estimate]) = this.get(userId).get

  def get(userId: String): Option[(User, Option[Estimate])] = {
    val oUser = users.find(_.id == userId)
    oUser.map(user => (user, estimates(user.id)))
  }

  def +(user: User): Room = {
    val mUsers = users + user
    val mEstimates = estimates + (user.id -> None)
    this.copy(users = mUsers, estimates = mEstimates)
  }

  def -(userId: String): Room = {
    val mUsers = users.filterNot(_.id == userId)
    val mEstimates = estimates - userId
    this.copy(users = mUsers, estimates = mEstimates)
  }

  def contains(userId: String): Boolean = users.exists(_.id == userId)

  def withEstimate(userId: String, estimate: Estimate): Room Or UnauthorizedErr =
    if (estimates.contains(userId)) {
      val updatedEstimates = estimates + (userId -> Some(estimate))
      Good(this.copy(estimates = updatedEstimates))
    } else {
      Bad(UnauthorizedErr("Only a member of a room may submit an estimate to it"))
    }

  def clearedBy(userId: String): Room Or UnauthorizedErr =
    if (userId == ownerId) {
      val clearedEstimates = estimates.mapValues(_ => None)
      Good(this.copy(estimates = clearedEstimates))
    } else {
      Bad(UnauthorizedErr("Only the room owner may clear the room's estimates"))
    }

  def revealedBy(userId: String): Room Or UnauthorizedErr =
    if (userId == ownerId) {
      Good(this.copy(isRevealed = true))
    } else {
      Bad(UnauthorizedErr("Only the room owner may reveal the room's estimates."))
    }
}

case class RoomInfo(id: String, ownerId: String, isRevealed: Boolean)

object RoomInfo {
  implicit val writer: OWrites[RoomInfo] = OWrites[RoomInfo] {
    case RoomInfo(id, ownerId, isRevealed) =>
      Json.obj(
        "id" -> id,
        "ownerId" -> ownerId,
        "isRevealed" -> isRevealed
      )
  }
}
