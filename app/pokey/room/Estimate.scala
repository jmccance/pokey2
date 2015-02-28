package pokey.room

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Estimate(value: Option[String], comment: Option[String])

object Estimate {
  implicit val formatter =
    ((JsPath \ "value").formatNullable[String]
      and (JsPath \ "comment").formatNullable[String])(Estimate.apply, unlift(Estimate.unapply))
}
