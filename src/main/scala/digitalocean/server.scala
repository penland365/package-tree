package digitalocean
package packagetree

import digitalocean.packagetree.messages.{Error, Message}
import java.net.{ServerSocket, Socket, SocketException}
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.concurrent.{ExecutorService, Executors, CountDownLatch}
import scala.util.{Failure, Success, Try}

object server {

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
  
  private final class ConnectionHandler(socket: Socket) extends Runnable {
  
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
  
    private def printErrorMessage(throwable: Throwable): Unit = {
      val timestamp = LocalDateTime.now()
      val thread    = Thread.currentThread.getName()
      println(s"$timestamp::[ERROR]::[$thread]::${throwable.getMessage}")
    }
  }
}
