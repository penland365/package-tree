package digitalocean
package packagetree

import java.util.concurrent.ConcurrentHashMap
import scala.collection._
import scala.collection.convert.decorateAsScala._
import digitalocean.packagetree.messages._

object PackageTree {

  private val packages: concurrent.Map[String, Set[String]] = new ConcurrentHashMap().asScala

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
}
