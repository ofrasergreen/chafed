package chafe
import scala.xml.NodeSeq
import java.util.Date
import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
import java.io.BufferedReader
import java.io.Writer
import java.io.InputStream
import java.io.InputStreamReader
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream


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
    def parseContent(inputStreamReader: InputStreamReader) = {
      val reader = new BufferedReader(inputStreamReader)
      val parserFactory = new SAXFactoryImpl
      val parser = parserFactory.newSAXParser
      val source = new org.xml.sax.InputSource(reader)
      val adapter = new scala.xml.parsing.NoBindingFactoryAdapter
      val content = adapter.loadXML(source, parser)
      reader.close
      inputStreamReader.close
      content
    }
    
    def charset(contentType: String) = {
      try {
        val re = """.*charset=([^()<>@,;:\"/\[\]?={}\s]*).*""".r
        val re(charSet) = contentType
        Some(charSet)
      } catch {
        case t: Throwable => None
      }
    }
    
    def parse(newReq: Request)(responseCode: Int, responseMessage: String, responseHeaders: List[Header], inputStream: InputStream): Response = {
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
        val content = charset(responseHeaders.find(_.name == "Content-Type").map(_.value).getOrElse("")) match {
          case Some(charset) =>
            // There was a charset in the Content-Type header
            parseContent(new InputStreamReader(inputStream, charset))
          case _ =>
            // Don't know the charset so let's try and parse it and see if there's a <meta http-equiv="content-type"> in there

            // Read the content into a bytearray because we may need to re-read it.
            val buffer = new ByteArrayOutputStream
            val data = new Array[Byte](16384)
            var n = 0
            while ({n = inputStream.read(data, 0, data.length); n != -1}) {
              buffer.write(data, 0, n)
            }
            buffer.flush()
            
            val is = new ByteArrayInputStream(buffer.toByteArray)
            is.mark(buffer.size)
            
            val content = parseContent(new InputStreamReader(is))
            (for {
              meta <- content \\ "meta"
              contentType <- meta \ "@content" if ((meta \ "@http-equiv").text.toLowerCase == "content-type")
              cs <- charset(contentType.text)
            } yield {
              // We've found a charset in the meta tags
              is.reset
              parseContent(new InputStreamReader(is, cs))
            }).headOption.getOrElse(content)
        }
        HtmlResponse(new Html(content, context), context)
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

