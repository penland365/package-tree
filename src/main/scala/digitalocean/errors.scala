package digitalocean
package packagetree

/** Holds all types to represent applications errors within the Application */
object errors {

  private[packagetree] class PackageTreeError(message: String) extends Exception {
    override def getMessage: String = message
  }

  /** Represents the failure to decode a Message 
    * @constructor create a decode failure with the given message
    * @param message the reason the protocol was invalid
    */
  final class InvalidProtocolError(message: String) extends PackageTreeError(message)
}
