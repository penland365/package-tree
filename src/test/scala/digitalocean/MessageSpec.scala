package digitalocean
package packagetree

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.{Specification, ScalaCheck}
import digitalocean.packagetree.messages._

final class MessageSpec extends Specification with ScalaCheck { def is = s2"""

  Response
    OK must return correct bytes     $testOkBytes
    FAIL must return correct bytes   $testFailBytes
    ERROR must return correct bytes  $testErrorBytes


                                                                           """

  def testOkBytes()    = new String(Ok.asBytes())    must_== "OK\n"
  def testFailBytes()  = new String(Fail.asBytes())  must_== "FAIL\n"
  def testErrorBytes() = new String(Error.asBytes()) must_== "ERROR\n"
}
