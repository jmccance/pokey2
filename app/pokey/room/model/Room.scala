package pokey.room.model

import org.scalactic.{ Bad, Good, Or }
import play.api.libs.json.{ Json, OWrites }
import pokey.common.error.UnauthorizedErr
import pokey.user.model.User

case class Room(
    id: String,
    ownerId: String,
    topic: String,
    isRevealed: Boolean = false,
    private val usersById: Map[String, User] = Map.empty,
    estimates: Map[String, Option[Estimate]] = Map.empty
) {
  lazy val users = usersById.values
  lazy val roomInfo: RoomInfo = RoomInfo(id, ownerId, topic, isRevealed)

  lazy val publicEstimates: Map[String, Option[PublicEstimate]] =
    if (isRevealed) estimates.mapValues(_.map(_.asRevealed))
    else estimates.mapValues(_.map(_.asHidden))

  def apply(userId: String): (User, Option[Estimate]) = this.get(userId).get

  def get(userId: String): Option[(User, Option[Estimate])] = {
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

  def -(userId: String): Room = {
    val mUsers = usersById - userId
    val mEstimates = estimates - userId
    this.copy(usersById = mUsers, estimates = mEstimates)
  }

  def contains(userId: String): Boolean = usersById.contains(userId)

  def withEstimate(userId: String, estimate: Estimate): Room Or UnauthorizedErr =
    if (estimates.contains(userId)) {
      val updatedEstimates = estimates + (userId -> Some(estimate))
      Good(this.copy(estimates = updatedEstimates))
    } else {
      Bad(UnauthorizedErr("Only a member of a room may submit an estimate to it."))
    }

  def clearedBy(userId: String): Room Or UnauthorizedErr =
    if (userId == ownerId) {
      val clearedEstimates = estimates.mapValues(_ => None)
      Good(this.copy(estimates = clearedEstimates, isRevealed = false))
    } else {
      Bad(UnauthorizedErr("Only the room owner may clear the room's estimates."))
    }

  def revealedBy(userId: String): Room Or UnauthorizedErr =
    if (userId == ownerId) {
      Good(this.copy(isRevealed = true))
    } else {
      Bad(UnauthorizedErr("Only the room owner may reveal the room's estimates."))
    }

  def topicSetBy(userId: String, topic: String): Room Or UnauthorizedErr =
    if (userId == ownerId) {
      Good(this.copy(topic = topic))
    } else {
      Bad(UnauthorizedErr("Only the room owner may set the room topic."))
    }
}

case class RoomInfo(id: String, ownerId: String, topic: String, isRevealed: Boolean)

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
