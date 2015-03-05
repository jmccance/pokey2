package pokey.common.error

trait Err {
  val message: String
}

case class UnauthorizedErr(message: String) extends Err
