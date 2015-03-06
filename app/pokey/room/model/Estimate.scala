package pokey.room.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Estimate(value: Option[String], comment: Option[String]) {
  lazy val asRevealed = RevealedEstimate(value, comment)
  lazy val asHidden = HiddenEstimate
}

trait PublicEstimate

case class RevealedEstimate(value: Option[String], comment: Option[String]) extends PublicEstimate
case object HiddenEstimate extends PublicEstimate

object Estimate {
  implicit val formatter =
    ((JsPath \ "value").formatNullable[String]
      and (JsPath \ "comment").formatNullable[String])(Estimate.apply, unlift(Estimate.unapply))
}
