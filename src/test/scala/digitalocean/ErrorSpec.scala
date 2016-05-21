package digitalocean
package packagetree

import digitalocean.packagetree.errors.InvalidProtocolError
import org.scalacheck.Prop.forAll
import org.specs2.{Specification, ScalaCheck}

final class ErrorSpec extends Specification with ScalaCheck { def is = s2"""

  Errors
    InvalidProtocolError must have correct message  $testInvalidProtocolError

                                                                           """

  val testInvalidProtocolError = forAll { x: String =>
    val error = new InvalidProtocolError(x)
    error.getMessage must_== x
  }
}
