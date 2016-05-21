package digitalocean
package packagetree

object errors {

  private[packagetree] class PackageTreeError(message: String) extends Exception {
    override def getMessage: String = message
  }

  final class InvalidProtocolError(message: String) extends PackageTreeError(message)
}
