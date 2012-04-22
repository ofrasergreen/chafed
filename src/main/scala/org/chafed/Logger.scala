package org.chafed

  /**
   * The logging interface. The intention is that consumers of this library
   * wishing to log Chafe messages using their own logging system can do so
   * by providing a custom implementation of this interface.
   */
trait Logger {
  /**
   * Log something.
   */
  def log(msg: => String)
}

/**
 * The "null logger" doesn't do anything at all.
 */
object NullLogger extends Logger {
  def log(msg: => String) {
    // Do nothing. That's right, that's why it's called the null logger!
  }
}

  
/**
 * The printf logger just prints all log messages to stdout.
 */
object PrintfLogger extends Logger {
  def log(msg: => String) {
    println(msg)
  }
}
