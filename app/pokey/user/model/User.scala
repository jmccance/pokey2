package pokey.user.model

import play.api.libs.json._

case class User(id: String, name: String)

object User {
  implicit val writes: OWrites[User] = OWrites[User] {
    case User(id, name) =>
      Json.obj(
        "id" -> id,
        "name" -> name
      )
  }
}
