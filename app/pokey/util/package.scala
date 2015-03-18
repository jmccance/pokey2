package pokey

package object util {
  def using[T](instance: T)(f: T => Unit): T = {
    f(instance)
    instance
  }

  def uidStream: Stream[String] = Stream.continually(new java.rmi.server.UID().toString)
}
