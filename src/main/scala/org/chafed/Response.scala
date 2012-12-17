package org.chafed
import java.io.Serializable

abstract class Response()(implicit val logger: Logger) {
  protected val context: Context  
  
  //val body: scala.xml.NodeSeq = Nil
  
  def isEmpty: Boolean = true
  def isDefined: Boolean = !isEmpty
  
  def get: Html
  
  def getOrElse(default: => Html): Html = default
  
  /**
   * Determine whether this Response contains a value which satisfies the predicate
   * @param func the predicate to test
   * @return true if the Response contains and the predicate is satisfied
   */
  def exists(func: Html => Boolean): Boolean = false
  
  def iterator: Iterator[Html] = Iterator.empty
  
  def foreach[U](f: Html => U): Unit = {}
  
  def toList: List[Html] = Nil
  
  def resource = context.request.resource

  def cookies = context.cookies
  
  override def toString = if (isEmpty) "" else get.toString
  
  def log = {
    logger.log(toString)
    this
  }
}

object Response {
  implicit def response2List(r: Response) = r.toList
  implicit def response2UserAgent(r: Response) = new UserAgent(r.context, r.logger)
}

final case class HtmlResponse(html: Html, context: Context)(override implicit val logger: Logger) extends Response {
  override def isEmpty: Boolean = false  
    
  override def get: Html = html
  
  override def getOrElse(default: => Html): Html = html
  
  override def exists(func: Html => Boolean): Boolean = func(html)
  
  override def foreach[U](f: Html => U): Unit = f(html)
  
  override def toList: List[Html] = html :: Nil
}

final case class ErrorResponse(code: Int, message: String, headers: List[Header], context: Context)(override implicit val logger: Logger) extends Response {
  override def isEmpty: Boolean = true
  
  override def get: Html = throw new Exception("Calling get() on an ErrorResponse")
}

final case class RedirectResponse(code: Int, location: String, context: Context)(override implicit val logger: Logger) extends Response {
  override def isEmpty: Boolean = true
  
  override def get: Html = throw new Exception("Calling get() on a RedirectResponse")
}


final case class NilResponse(headers: List[Header])(override implicit val logger: Logger) extends Response {
  val context =  Context(Request.Nil.copy(headers=headers), Nil)
  
  override def get: Html = throw new Exception("Calling get() on the NilResponse")
}

final case class InvalidRequest(message: String, context: Context)(override implicit val logger: Logger) extends Response {
  override def isEmpty: Boolean = true
  
  override def get: Html = throw new Exception("Calling get() on an InvalidRequest") 
}
