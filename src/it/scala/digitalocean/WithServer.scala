package digitalocean
package packagetree

import org.specs2.Specification
import org.specs2.specification.core.Fragments
import org.specs2.specification.dsl.FragmentsDsl
import org.specs2.specification.Step
import sys.process.Process

trait WithServer extends Specification {

  override def map(fragments: => Fragments) = step(beforeAll) ^ fragments ^ step(afterAll)

  protected var server: Process = null 
  private def beforeAll(): Unit = {
    val cmd = Process("target/universal/stage/bin/packagetree 100")
    server = cmd.run
    Thread.sleep(2000) // we need to give the JVM a couple of secondds to turn on
  }
  private def afterAll(): Unit = {
    server.destroy()
  }
}
