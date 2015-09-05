package pokey

package object util {
  def using[T](instance: T)(f: T => Unit): T = {
    f(instance)
    instance
  }

  def uidStream: Stream[String] = {
    val encBase64 = java.util.Base64.getEncoder.encode(_: Array[Byte])

    Stream.continually(java.util.UUID.randomUUID().toString)
  }
}
