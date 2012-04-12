package chafe
import scala.xml.NodeSeq
import java.util.Date
import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
import java.io.BufferedReader
import java.io.Writer


case class Cookie(
    name: Option[String],
    value: String,
    domain: String,
    path: String,
    expires: Option[Date],
    secure: Boolean,
    httpOnly: Boolean
    )

case class Context(
    val request: Request,
    val cookies: List[Cookie],
    maxDepth: Int = 10
    ) {
  
  private def extractCookies(url: URL, headers: List[Header]) = headers.filter(_.name == "Set-Cookie").flatMap { header =>
    object dateFormat extends java.text.SimpleDateFormat("EEE, dd-MMM-yyyy kk:mm:ss zzz")
    
    header.value.split(";").toList match {
      case nameValue :: attrs =>
        val attrMap = attrs.map(_.trim.split("=")).map(kv => (kv(0).toLowerCase, (if (kv isDefinedAt 1) kv(1) else ""))).toMap
        // Split the name-value if they are separate
        val (name, value) = nameValue.split("=").toList match {
          case n :: v :: Nil => (Some(n), v)
          case _ => (None, nameValue)
        }
        List(Cookie(
            name,
            value, 
            attrMap.getOrElse("domain", url.host),
            attrMap.getOrElse("path", url.path),
            attrMap.get("expires").flatMap(e => try { Some(dateFormat.parse(e)) } catch { case t: Throwable => None }),
            attrMap.contains("secure"),
            attrMap.contains("httponly")
            ))
      case _ => Nil 
    }
  }
  
  def invoke(req: Request): Response = {
    def parse(newReq: Request)(responseCode: Int, responseMessage: String, responseHeaders: List[Header], reader: BufferedReader): Response = {
      // Parse the cookies
      val cookieKeys = scala.collection.mutable.Set[String]()
      val newCookies = (extractCookies(newReq.resource, responseHeaders) ++ cookies).filter { cookie =>
        val valid = (!(cookie.name.isDefined && cookieKeys.contains(cookie.name.get)) &&
          !(cookie.expires.isDefined && cookie.expires.get.after(new java.util.Date)))
        if (cookie.name.isDefined) cookieKeys += cookie.name.get
        valid
      }
      
      // This is our context to use next time
      val context = Context(newReq, newCookies)
      
      if (responseCode >= 200 && responseCode < 299) {
        // OK. Parse it 
        val parserFactory = new SAXFactoryImpl
        val parser = parserFactory.newSAXParser
        val source = new org.xml.sax.InputSource(reader)
        val adapter = new scala.xml.parsing.NoBindingFactoryAdapter
        HtmlResponse(new Html(adapter.loadXML(source, parser), context), context)
      } else if (responseCode >= 301 && responseCode <= 303) {
        val location = responseHeaders.filter(_.name == "Location").map(_.value).headOption.getOrElse(
            throw new Exception("Server returned a redirect without setting Location."))
        RedirectResponse(responseCode, location, context.copy(maxDepth = maxDepth - 1))
      } else {
        ErrorResponse(responseCode, responseMessage, responseHeaders, context)
      }
    }
    
    if (maxDepth < 1) throw new Exception("Redirection loop detected.")
    
    // Fetch the document
    val newRequest = req.merge(request, cookies)
    Client(newRequest.resource.scheme).fetch(newRequest)(parse(newRequest)) match { 
      case r: RedirectResponse => r.GET(r.location)
      case response => response
    }
  }
  
  def refresh() = {
    
  }
}

