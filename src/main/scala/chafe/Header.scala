package chafe

/**
 * A request header.
 */
private[chafe] case class Header(name: String, value: String) {
  override def toString = name + ": " + value
}
