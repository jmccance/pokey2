package pokey.user.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class User(id: User.Id, name: User.Name)

object User {
  def unsafeFrom(id: String, name: String): User = User(Id.unsafeFrom(id), Name.unsafeFrom(name))

  implicit val writes: OWrites[User] = OWrites[User] {
    case User(id, name) =>
      Json.obj(
        "id" -> id,
        "name" -> name)
  }

  abstract case class Id private (value: String)

  object Id {
    def from(s: String): Option[Id] =
      Option(s)
        .filter(_.nonEmpty)
        .map(new Id(_) {})

    def unsafeFrom(s: String): Id = new Id(s) {}

    implicit val writesId: Writes[Id] = Writes.of[String].contramap(_.value)

    implicit def writesMapId[A: Writes]: Writes[Map[Id, A]] =
      Writes.of[Map[String, A]].contramap(_.map {
        case (k, v) => (k.value, v)
      }.toMap)
  }

  abstract case class Name private (value: String)

  object Name {
    def from(s: String): Option[Name] =
      Option(s)
        .filter(_.nonEmpty)
        .map(new Name(_) {})

    def unsafeFrom(s: String): Name = new Name(s) {}

    implicit val writesName: Writes[Name] = Writes.of[String].contramap(_.value)

    implicit val readsName: Reads[Name] = Reads.of[String].map(Name.from).flatMap {
      case Some(name) => Reads.pure(name)
      case None => Reads(_ => JsError("error.user.name.empty"))
    }
  }
}
