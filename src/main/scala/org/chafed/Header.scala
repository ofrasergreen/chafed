package org.chafed

/**
 * A request header.
 */
private[chafed] case class Header(name: String, value: String) {
  override def toString = name + ": " + value
}
