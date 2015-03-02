package pokey.room

import pokey.user.User

import scala.util.{Failure, Success, Try}

case class Room(id: String,
                ownerId: String,
                users: Set[User] = Set.empty,
                state: RoomState = RoomState.default) {
  def +(user: User): Room = {
    val newUsers = users + user
    val newState = state + user
    this.copy(users = newUsers, state = newState)
  }
  
  def -(user: User): Room = {
    val newUsers = users - user
    val newState = state - user
    this.copy(users = newUsers, state = newState)
  }

  def contains(userId: String): Boolean = users.exists(_.id == userId)
  
  def withEstimate(userId: String, estimate: Estimate): Try[Room] = 
    state
      .withEstimate(userId, estimate)
      .map(newState => this.copy(state = newState))
  
  def cleared(): Room = this.copy(state = state.cleared())

  def revealed(): Room = this.copy(state = state.revealed())
}

case class RoomState(isRevealed: Boolean, estimates: Map[String, Option[Estimate]]) {
  def +(user: User): RoomState = this.copy(estimates = estimates + (user.id -> None))

  def -(user: User): RoomState = this.copy(estimates = estimates - user.id)

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
