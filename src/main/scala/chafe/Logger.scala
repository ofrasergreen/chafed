package chafe

  /**
   * The logging interface. The intention is that consumers of this library
   * wishing to log Chafe messages using their own logging system can do so
   * by providing a custom implementation of this interface.
   */
trait Logger {
  /**
   * Log something. ctx is an integer identifying the UserAgent.
   */
  def log(ctx: => Int, str: => String)
}

/**
 * The "null logger" doesn't do anything at all.
 */
object NullLogger extends Logger {
  def log(ctx: => Int, str: => String) {
    // Do nothing. That's right, that's why it's called the null logger!
  }
}

  
/**
 * The printf logger just prints all log messages to stdout.
 */
object PrintfLogger extends Logger {
  def log(ctx: => Int, str: => String) {
    println("%08x: %s".format(ctx, str))
  }
}
