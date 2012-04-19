package chafe

import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
import java.io.BufferedReader
import java.io.Writer
import scala.util.matching.Regex

class UserAgent(
    val context: Context = Context.newWithIdentity("Chafe/1.0"),
    val logger: Logger = NullLogger) {
  implicit val theLogger = logger
  
  private def extractCookies(url: URL, headers: List[Header]) = headers.filter(_.name == "Set-Cookie").flatMap { header =>
    object dateFormat extends java.text.SimpleDateFormat("EEE, dd-MMM-yyyy kk:mm:ss zzz")
    
    header.value.split(";").toList match {
      case nameValue :: attrs =>
        val attrMap = attrs.map(_.trim.split("=")).map(kv => (kv(0).toLowerCase, (if (kv isDefinedAt 1) kv(1) else ""))).toMap
        // Split the name-value if they are separate
        val (name, value) = nameValue match {
          case UserAgent.cookieRE(n, v) => (Some(n), v)
          case _ => (None, nameValue)
        }
        val cookie = Cookie(
            name,
            value, 
            attrMap.getOrElse("domain", url.host),
            attrMap.getOrElse("path", url.path),
            attrMap.get("expires").flatMap(e => try { Some(dateFormat.parse(e)) } catch { case t: Throwable => None }),
            attrMap.contains("secure"),
            attrMap.contains("httponly")
            )
        logger.log("Setting cookie: " + cookie)
        List(cookie)
      case _ => Nil 
    }
  }
  
  private def invoke(req: Request): Response = {
    def parse(newReq: Request)(responseCode: Int, responseMessage: String, 
      responseHeaders: List[Header], reader: BufferedReader): Response = {
      logger.log("<<< " + context + " received response " + 
          responseCode + " " + responseMessage + ":")
      logger.log(responseHeaders.map("< " + _.toString + "\n").mkString(""))
      
      // Parse the cookies
      val cookieKeys = scala.collection.mutable.Set[String]()
      val newCookies = (extractCookies(newReq.resource, responseHeaders) ++ context.cookies).filter { cookie =>
        val valid = (!(cookie.name.isDefined && cookieKeys.contains(cookie.name.get)) &&
          !(cookie.expires.isDefined && cookie.expires.get.before(new java.util.Date)))
        if (cookie.name.isDefined) cookieKeys += cookie.name.get
        valid
      }
      
      // This is our context to use next time
      val newContext = Context(newReq, newCookies)
      
      if (responseCode >= 200 && responseCode < 299) {
        // OK. Parse it 
        val parserFactory = new SAXFactoryImpl
        val parser = parserFactory.newSAXParser
        val source = new org.xml.sax.InputSource(reader)
        val adapter = new scala.xml.parsing.NoBindingFactoryAdapter
        HtmlResponse(new Html(adapter.loadXML(source, parser), newContext), newContext)
      } else if (responseCode >= 301 && responseCode <= 303) {
        val location = responseHeaders.filter(_.name == "Location").map(_.value).headOption.getOrElse(
            throw new Exception("Server returned a redirect without setting Location."))
        RedirectResponse(responseCode, location, newContext.copy(maxDepth = newContext.maxDepth - 1))
      } else {
        ErrorResponse(responseCode, responseMessage, responseHeaders, context)
      }
    }
    
    if (context.maxDepth < 1) throw new Exception("Redirection loop detected.")
    
    // Fetch the document
    val newRequest = req.merge(context.request, context.cookies)
    logger.log(">>> " + context + " sending request:")
    logger.log(newRequest.toString)
    Client(newRequest.resource.scheme).fetch(newRequest)(parse(newRequest)) match { 
      case r: RedirectResponse =>
        logger.log("Redirecting")
        r.GET(r.location)
      case r => r
    }
  }

  import URLBuilder._
 
  def GET(uri: URI, headers: Header*) = invoke(Request(Get, uri.toURL(context.request.resource), headers.toList))
  def POST(uri: URI, inputs: Map[String, Option[String]], headers: Header*) = invoke(Request(Post, uri.toURL(context.request.resource), headers.toList, FormURLEncoded(inputs)))
}

object UserAgent extends UserAgent(Context.newWithIdentity("Chafe/1.0"), NullLogger) {
  def apply(identity: String) = new UserAgent(Context.newWithIdentity(identity))
  
  def chrome = UserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1")
  def safari = UserAgent("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/312.8 (KHTML, like Gecko) Safari/312.3.32011-10-10 14:01:24")
  def firefox = UserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.23) Gecko/20110920 Firefox/3.6.232011-10-10 14:01:11")
  def ie9 = UserAgent("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 7.1; Trident/5.0)")
  def iPad = UserAgent("Mozilla/5.0 (iPad; U; CPU OS 4_3_5 like Mac OS X; sv-se) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.52011-10-10 14:01:20")
  def android = UserAgent("Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1")
  
  protected def cookieRE = new Regex("""([^=]*)=(.*)""")
}