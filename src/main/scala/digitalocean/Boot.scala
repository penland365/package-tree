package digitalocean
package packagetree

import digitalocean.packagetree.server.PackageServer
import java.util.concurrent.CountDownLatch
import scala.util.{Failure, Success, Try}

object Boot {
  private[this] val DefaultPoolSize = 100
  private[this] val DefaultPort     = 8080

  def main(args: Array[String]): Unit = {
    val (poolSize, port) = parseThreadsPort(args)

    val done = new CountDownLatch(1)
    val packageServer = new PackageServer(port, poolSize)
    packageServer.run()
    done.await()
  }

  private def parseThreadsPort(args: Array[String]): (Int, Int) = {
    val nThreads = Try(args.head.toInt) match {
      case Success(x) => x
      case Failure(_) => DefaultPoolSize
    }
    val port = Try(args(1).toInt) match {
      case Success(x) => x
      case Failure(_) => DefaultPort
    }
    (nThreads, port)
  }
}
