package chafe
import java.io.Serializable

sealed abstract class Response extends Serializable with Selectable {
  protected val context: Context  
  
  protected val content: scala.xml.NodeSeq = Nil
  
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
  
  def toOption: Option[Html] = None
  
  def resource = context.request.resource
  
  import URLBuilder._
  
  def GET(uri: URI, headers: Header*) = context.invoke(Request(Get, uri.toURL(context.request.resource), headers.toList))
}

object Response {
  implicit def response2Option(r: Response) = r.toOption
}

final case class HtmlResponse(body: Html, context: Context) extends Response {
  override def isEmpty: Boolean = false  
  
  override protected val content: scala.xml.NodeSeq = body.content
  
  override def get: Html = body
  
  override def getOrElse(default: => Html): Html = body
  
  override def exists(func: Html => Boolean): Boolean = func(body)
  
  override def foreach[U](f: Html => U): Unit = f(body)
  
  override def toList: List[Html] = body :: Nil
  
  override def toOption: Option[Html] = Some(body)
}

final case class ErrorResponse(code: Int, message: String, headers: List[Header], context: Context) extends Response {
  override def isEmpty: Boolean = true
  
  override def get: Html = throw new Exception("Calling get() on an ErrorResponse")
}

final case class RedirectResponse(code: Int, location: String, context: Context) extends Response {
  override def isEmpty: Boolean = true
  
  override def get: Html = throw new Exception("Calling get() on a RedirectResponse")
}


class NilResponse(headers: List[Header]) extends Response {
  val context =  Context(Request.Nil.copy(headers=headers), Nil)
  
  override def get: Html = throw new Exception("Calling get() on the NilResponse")
}
