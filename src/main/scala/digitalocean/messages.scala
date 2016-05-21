package digitalocean
package packagetree

import java.nio.charset.StandardCharsets
import scala.util.Try
import digitalocean.packagetree.errors.InvalidProtocolError

object messages {

  sealed abstract class Command
  final case object Index extends Command
  final case object Remove extends Command
  final case object Query extends Command

  case class Message(command: Command, pkg: String, dependencies: List[String])
  object Message {
    private[packagetree] val Delimiter = '|' // private to packagetree for testing

    def apply(xs: String): Try[Message] = Try({
      val pipeCount = xs.foldLeft(0)((i,j) => j match {
        case Delimiter => i + 1
        case _         => i
      })
      if(pipeCount != 2) 
        throw new InvalidProtocolError(s"Invalid delimiter count - $pipeCount delimiters found.")
      val command = xs.takeWhile(_ != Delimiter ).mkString match {
        case "INDEX"  => Index
        case "REMOVE" => Remove
        case "QUERY"  => Query
        case cmd      => throw new InvalidProtocolError(s"Unknown command $cmd")
      }
      val firstIdx = xs.indexOf(Delimiter)
      val lastIdx  = xs.lastIndexOf(Delimiter)
      val pkg = xs.slice(firstIdx + 1, lastIdx).mkString
      val depLength = xs.length - (lastIdx + 1)
      val dependencies = depLength match {
        case x if x > 1  => xs.takeRight(xs.length - (lastIdx + 1))
          .mkString
          .stripLineEnd
          .split(",")
          .toList
        case _           => List.empty[String]
      }

      Message(command, pkg, dependencies)
    })
  }

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
