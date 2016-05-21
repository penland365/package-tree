package digitalocean
package packagetree

import java.nio.charset.StandardCharsets

object messages {


  sealed abstract class Response() {
    private[this] val okBytes    = "OK\n".getBytes(StandardCharsets.UTF_8)
    private[this] val failBytes  = "FAIL\n".getBytes(StandardCharsets.UTF_8)
    private[this] val errorBytes = "ERROR\n".getBytes(StandardCharsets.UTF_8)

    def asBytes(): Array[Byte] = this match {
      case `Ok`    => okBytes
      case `Fail`  => failBytes
      case `Error` => errorBytes
    }
  }
  final case object Ok extends Response
  final case object Fail extends Response
  final case object Error extends Response
}
