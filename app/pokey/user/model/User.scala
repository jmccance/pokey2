package pokey.user.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class User(id: String, name: String)

object User {
  implicit val format: Format[User] =
    ((JsPath \ "id").format[String]
      and (JsPath \ "name").format[String])(User.apply, unlift(User.unapply))
}
