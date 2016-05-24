package digitalocean
package packagetree

import java.util.concurrent.ConcurrentHashMap
import scala.collection._
import scala.collection.convert.decorateAsScala._
import digitalocean.packagetree.messages._

/** The Package Database. This database represents the source of truth for all package actions.
  *
  * ==Overview==
  * The PackageTree is designed to commit a [[messages.Message]] in a safe, concurrent way. 
  * Depending on * the underlying state of the database, the [[messages.Command]] may or may 
  * not be permitted. Currently, 3 commands are allowed, 
  * 
  *  1. QUERY
  *  1. INDEX
  *  1. REMOVE
  *
  * ==Rules Governing the PackageTree==
  *
  *  1. INDEX, the server returns [[messages.Ok]] if the package could be indexed or if it was 
  *    already present. It returns [[messages.Fail]] if the package cannot be indexed because some 
  *    of its dependencies aren't indexed yet and need to be installed first.
  *  1. QUERY, the server returns [[messages.Ok]] if the package is indexed. It returns 
  *    [[messages.Fail]] if the package isn't indexed.
  *  1. REMOVE, the server returns [[messages.Ok]] if the package could be removed. It returns 
  *    [[messages.Fail]] if the package could not be removed from the index because some other 
  *    indexed package depends on it. It returns [[messages.Ok]] if the package wasn't indexed.
  */
object PackageTree {

  private val packages: concurrent.Map[String, Set[String]] = new ConcurrentHashMap().asScala

  /** Commits a message to the database.
    *
    * This method may or may not impact the underlying database depending on if the command is 
    *   permitted.
    * @param a [[Message]]
    */
  def commit(message: Message): Response = message.command match {
    case `Index` => if(packages.contains(message.pkg)) {
      Ok
    } else {
      if(eligibleForIndex(message)) {
        packages.put(message.pkg, message.dependencies.toSet)
        Ok
      } else Fail
    }
    case `Remove` => if(eligibleForRemoval(message)) {
      packages.remove(message.pkg)
      Ok
    } else Fail
    case `Query` => if(packages.contains(message.pkg)) {
      Ok
    } else Fail
  }

  private def eligibleForRemoval(message: Message): Boolean = {
    val not = packages - message.pkg
    val allDeps = not.values.toSet.flatten
    !allDeps.contains(message.pkg)
  }

  private def eligibleForIndex(message: Message): Boolean = message.dependencies
    .map(packages.contains(_))
    .forall(_ == true)

  private[packagetree] def wipe(): Unit = packages.clear // method exposed for testing only
  private[packagetree] def all(): Unit =
    packages.foreach(x => print(s"$x "))
}
