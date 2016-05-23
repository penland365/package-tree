package digitalocean
package packagetree

import digitalocean.packagetree.messages._
import org.specs2.mutable.Specification
import scala.io.Source
import scala.util.Random

final class PackageTreeSpec extends Specification { 
  isolated

  override def is = s2""" 

  Index
    must return Ok if package is already indexed               $testIndexAlreadyPresent
    must return Ok if package is indexed                       $testIndex
    must return Fail if package's dependencies are not present $testIndexWithoutDeps

  Remove
    must return Ok if package is removed                                        $testRemoveOk
    must return Fail if package cannot be removed due to existing dependencies  $pending

  Query
    must return Fail when package isn't present  $testQueryFail
    must reurn Ok when package is present        $testQueryOk

                                                                               """


  def testIndexAlreadyPresent = {
    PackageTree.wipe
    val pkg  = randoPkg
    val indexMessage = new Message(Index, pkg, List.empty[String]) 
    PackageTree.commit(indexMessage)
    PackageTree.commit(indexMessage) must_== Ok
  }
  def testIndex = {
    PackageTree.wipe
    val pkg0 = randoPkg
    val pkg1 = randoPkg
    PackageTree.commit(new Message(Index, pkg0, List.empty[String]))
    PackageTree.commit(new Message(Index, pkg1, List.empty[String]))

    val pkg2 = randoPkg
    val deps = pkg0 :: pkg1 :: Nil
    PackageTree.commit(new Message(Index, pkg2, deps)) must_== Ok
  }
  def testIndexWithoutDeps = {
    PackageTree.wipe
    val pkg = randoPkg
    val deps = randoPkg :: randoPkg :: randoPkg :: Nil
    PackageTree.commit(new Message(Index, pkg, deps)) must_== Fail
  }

  def testRemoveOk = {
    PackageTree.wipe
    val pkg = randoPkg
    PackageTree.commit(new Message(Index, pkg, List.empty[String]))
    PackageTree.commit(new Message(Remove, pkg, List.empty[String])) must_== Ok
  }
  def testRemoveFail = {

    val pkg0 = randoPkg
    PackageTree.commit(new Message(Index, pkg0, List.empty[String]))
    val pkg1 = randoPkg
    val deps = pkg0 :: Nil
    PackageTree.commit(new Message(Index, pkg1, deps))

    val message = new Message(Remove, pkg0, List.empty[String])
    PackageTree.commit(message) must_== Fail
  }


  def testQueryFail = {
    PackageTree.wipe
    val message = new Message(Query, randoPkg, List(randoPkg)) 
    PackageTree.commit(message) must_== Fail
  }
  def testQueryOk = {
    PackageTree.wipe
    val pkg  = randoPkg
    val indexMessage = new Message(Index, pkg, List.empty[String])
    val queryMessage = new Message(Query, pkg, List.empty[String])
    PackageTree.commit(indexMessage)
    PackageTree.commit(queryMessage) must_== Ok
  }

  private def randoPkg: String = Random.shuffle(deps).head

  private val deps = Source.fromFile("src/test/resources/deps.txt")
    .getLines()
    .mkString
    .split(",")
    .toList
}
