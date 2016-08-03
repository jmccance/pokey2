package pokey.room.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Estimate(value: Option[String], comment: Option[String]) {
  lazy val asRevealed = RevealedEstimate(value, comment)
  val asHidden = HiddenEstimate
}

object Estimate {
  implicit val reader =
    ((JsPath \ "value").readNullable[String]
      and (JsPath \ "comment").readNullable[String])(Estimate.apply _)
}

trait PublicEstimate

object PublicEstimate {
  implicit val writer = OWrites[PublicEstimate] {
    case e: RevealedEstimate => RevealedEstimate.writer.writes(e)
    case HiddenEstimate => Json.obj()
  }
}

case class RevealedEstimate(value: Option[String], comment: Option[String]) extends PublicEstimate

object RevealedEstimate {
  val writer = OWrites[RevealedEstimate] {
    case RevealedEstimate(value, comment) =>
      Json.obj(
        "value" -> value,
        "comment" -> comment
      )
  }
}

case object HiddenEstimate extends PublicEstimate
