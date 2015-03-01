package pokey.room

import pokey.user.User

case class Room(id: String,
                ownerId: String,
                users: Set[User] = Set.empty,
                state: RoomState = RoomState.default) {
  def cleared(): Room = this.copy(state = state.cleared())

  def revealed(): Room = this.copy(state = state.revealed())
}

case class RoomState(isRevealed: Boolean, estimates: Map[String, Option[Estimate]]) {
  def cleared(): RoomState = RoomState(isRevealed = false, estimates.mapValues(_ => None))

  def revealed(): RoomState = this.copy(isRevealed = true)
}

object RoomState {
  val default = RoomState(isRevealed = false, Map.empty)
}
