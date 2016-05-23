package digitalocean
package packagetree

import digitalocean.packagetree.server.PackageServer
import org.specs2.Specification
import sys.process.Process

final class IntegrationSpec 
  extends Specification 
  with WithServer { def is = s2"""

  PackageTree
    must pass the Digital Ocean supplied test harness $test

                                                                            """

  def test() = {
    val cmd = System.getProperty("os.name") match {
      case "Mac OS X" => Process("src/it/resources/do-package-tree_darwin concurrency 100 -unluckiness 25")
      case _ => Process("src/it/resources/do-package-tree_linux concurrency 100 -unluckiness 25")
    }
    val output = cmd.run.exitValue
    output must_== 0
  }
}
