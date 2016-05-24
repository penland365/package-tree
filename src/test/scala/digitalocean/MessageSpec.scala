package digitalocean
package packagetree

import digitalocean.packagetree.errors.InvalidProtocolError
import digitalocean.packagetree.messages._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.{Specification, ScalaCheck}
import scala.io.Source
import scala.util.{Failure, Success}

final class MessageSpec extends Specification with ScalaCheck { def is = s2"""

  Messages
    must have a pipe | as a delimiter                                              $testDelimiter
    must decode a valid Message String                                             $testMessageDecode
    must decode a Failure when message string has an invalid number of delimiters  $testInvalidDelims
    must decode a Failure when message string has an invalid Command               $testInvalidCommand

  Response
    OK must return correct bytes     $testOkBytes
    FAIL must return correct bytes   $testFailBytes
    ERROR must return correct bytes  $testErrorBytes

                                                                           """

  def testDelimiter()  = Message.Delimiter must_== '|'
  val testMessageDecode = forAll { x: MessageContainer =>
    Message(x.msgString) must_== Success(x.message) 
  }
  val testInvalidDelims = forAll { x: DelimiterContainer =>
    Message(x.delims).isFailure must_== true
  }
  val testInvalidCommand = forAll(genInvalidMessageString) { x: String =>
    Message(x).isFailure must_== true
  }

  def testOkBytes()    = new String(Ok.asBytes())    must_== "OK\n"
  def testFailBytes()  = new String(Fail.asBytes())  must_== "FAIL\n"
  def testErrorBytes() = new String(Error.asBytes()) must_== "ERROR\n"

  private lazy val genCommand: Gen[Command] = Gen.oneOf(Index, Remove, Query)
  private val dependencies = Source.fromFile("src/test/resources/deps.txt")
    .getLines()
    .mkString
    .split(",")
    .toList
  private implicit lazy val arbitraryCommand: Arbitrary[Command] = 
    Arbitrary(genCommand)
  private lazy val genValidMessage: Gen[Message] = for {
    cmd  <- arbitrary[Command]
    pkg  <- Gen.oneOf(dependencies)
    deps <- Gen.someOf(dependencies) 
  } yield new Message(cmd, pkg, deps.toList)
  private implicit lazy val arbitraryMessage: Arbitrary[Message] = Arbitrary(genValidMessage)

  case class MessageContainer(msgString: String, message: Message)
  private implicit lazy val genValidMessageContainer: Gen[MessageContainer] = for {
    message <- arbitrary[Message]
  } yield {
    val cmdString = message.command match {
      case `Index`  => "INDEX"
      case `Remove` => "REMOVE"
      case `Query`  => "QUERY"
    }
    val depsString = makeDepsString(message.dependencies.reverse, "")
    val messageString = s"$cmdString|${message.pkg}|$depsString"
    MessageContainer(messageString, message)
  }
  @annotation.tailrec
  private def makeDepsString(xs: List[String], string: String): String = xs match {
    case h :: t => makeDepsString(t, s"$h,$string")
    case t      => string.dropRight(1) + "\n" 
  }
  private implicit lazy val arbitraryMessageContainer: Arbitrary[MessageContainer] =
    Arbitrary(genValidMessageContainer)

  private case class DelimiterContainer(delims: String, numDelims: Int)
  private lazy val genDelimiterContainer: Gen[DelimiterContainer] = for {
    numDelims <- Gen.choose(3, 4)
  } yield {
    val xs = List.fill(numDelims)("|")
    new DelimiterContainer(xs.mkString, numDelims)
  }
  private implicit lazy val arbitraryDelimiterContainer: Arbitrary[DelimiterContainer] = 
    Arbitrary(genDelimiterContainer)

  private lazy val genInvalidMessageString: Gen[String] = for {
    command <- arbitrary[String] suchThat (_.contains("|") == false)
    pkg     <- Gen.oneOf(dependencies)
    deps    <- Gen.someOf(dependencies)
  } yield {
    val depsString = makeDepsString(deps.toList.reverse, "")
    s"$command|$pkg|$depsString"
  }
}
