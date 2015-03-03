package pokey.room

import pokey.user.User

import scala.util.{Failure, Success, Try}

case class Room(id: String,
                ownerId: String,
                users: Set[User] = Set.empty,
                state: RoomState = RoomState.default) {
  def apply(userId: String): (User, Option[Estimate]) = this.get(userId).get

  def get(userId: String): Option[(User, Option[Estimate])] = {
    val oUser = users.find(_.id == userId)
    oUser.map(user => (user, state(user.id)))
  }

  def +(user: User): Room = {
    val newUsers = users + user
    val newState = state + user
    this.copy(users = newUsers, state = newState)
  }

  def -(userId: String): Room = {
    val newUsers = users.filterNot(_.id == userId)
    val newState = state - userId
    this.copy(users = newUsers, state = newState)
  }

  def isRevealed = state.isRevealed

  def contains(userId: String): Boolean = users.exists(_.id == userId)

  def withEstimate(userId: String, estimate: Estimate): Try[Room] = 
    state
      .withEstimate(userId, estimate)
      .map(newState => this.copy(state = newState))
  
  def cleared(): Room = this.copy(state = state.cleared())

  def revealed(): Room = this.copy(state = state.revealed())
}

case class RoomState(isRevealed: Boolean, estimates: Map[String, Option[Estimate]]) {
  def get(userId: String): Option[Option[Estimate]] = estimates.get(userId)

  def apply(userId: String): Option[Estimate] = this.get(userId).get

  def +(user: User): RoomState = if (!estimates.contains(user.id)) {
    this.copy(estimates = estimates + (user.id -> None))
  } else this

  def -(userId: String): RoomState = this.copy(estimates = estimates - userId)

  def withEstimate(userId: String, estimate: Estimate): Try[RoomState] = {
    if (estimates.contains(userId)) {
      val updatedEstimates = estimates + (userId -> Some(estimate))
      Success(this.copy(estimates = updatedEstimates))
    } else Failure(new NoSuchElementException(userId))
  }

  def cleared(): RoomState = RoomState(isRevealed = false, estimates.mapValues(_ => None))

  def revealed(): RoomState = this.copy(isRevealed = true)
}

object RoomState {
  val default = RoomState(isRevealed = false, Map.empty)
}
