package digitalocean

import digitalocean.packagetree.messages.{Error, Message}
import digitalocean.packagetree.PackageTree
import java.net.{ServerSocket, Socket, SocketException}
import java.nio.charset.StandardCharsets
import java.util.concurrent.{ExecutorService, Executors, CountDownLatch}
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

final class PackageServer(port: Int, poolSize: Int) extends Runnable {
  private val serverSocket = new ServerSocket(port)
  private val pool: ExecutorService = Executors.newFixedThreadPool(poolSize)

  def run(): Unit = try {
    while (true) {
      val socket = serverSocket.accept()
      pool.execute(new ConnectionHandler(socket))
    }
  } finally {
    pool.shutdown()
  }
}

final class ConnectionHandler(socket: Socket) extends Runnable {

  def run(): Unit = try {
    while(true) {
      val inBytes       = Array.fill[Byte](2048)(0x0)
      val bytesRead     = socket.getInputStream.read(inBytes)
      val messageString = new String(inBytes.take(bytesRead), StandardCharsets.UTF_8)
      val outBytes      = Message(messageString) match {
        case Success(message) => PackageTree.commit(message).asBytes
        case Failure(ex)      => {
          printErrorMessage(ex)
          Error.asBytes
        }
      }
      socket.getOutputStream.write(outBytes)
      socket.getOutputStream.flush()
    }
  } catch {
    case se: SocketException => printErrorMessage(se)
  } finally {
    socket.getOutputStream.close()
  }

  private def printErrorMessage(throwable: Throwable): Unit =
    println(s"{Thread.currentThread.getName()} - ${throwable.getMessage}")
}
