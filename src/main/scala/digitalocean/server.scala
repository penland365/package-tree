package digitalocean
package packagetree

import digitalocean.packagetree.messages.{Error, Message}
import java.net.{ServerSocket, Socket, SocketException}
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.concurrent.{ExecutorService, Executors, CountDownLatch}
import scala.util.{Failure, Success, Try}

/** Contains the network handling classes for the Application */
object server {

  /** A PackageServer which manages the network for our Application
    * @constructor creates a Server that binds to the provided port, and an executor service
    *  to submit work to
    * @param port the port to Bind to
    * @param poolSize the fixed size of the Thread Pool to create
    */
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
  
  /** Handles each individual client connection.
    * @socket The Socket this connection is bound to
    */
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
