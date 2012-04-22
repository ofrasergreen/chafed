package org.chafed.header

import org.chafed.Header

trait NamedHeader {
  val name: String
}
trait StringHeader extends NamedHeader {
  def apply(value: String) = Header(name, value)
}

trait IntHeader extends NamedHeader {
  def apply(value: Int) = Header(name, value.toString)
}

object Accept extends StringHeader {
  val name = "Accept" 
}

object AcceptCharset extends StringHeader {
  val name = "Accept-Charset" 
}

object AcceptEncoding extends StringHeader {
  val name = "Accept-Encoding" 
}

object AcceptLanguage extends StringHeader { 
  val name = "Accept-Language" 
}

object Authorizaion extends StringHeader { 
  val name = "Authorization" 
}

object CacheControl extends StringHeader { 
  val name = "Cache-Control" 
}

object Connection extends StringHeader { 
  val name = "Connection" 
}

object Cookie extends StringHeader { 
  val name = "Cookie" 
}

object ContentLength extends IntHeader { 
  val name = "Content-Length" 
}

object ContentMD5 extends StringHeader { 
  val name = "Content-MD5" 
}

object ContentType extends StringHeader { 
  val name = "Content-Type" 
}

object Date extends StringHeader { 
  val name = "Date" 
}

object Expect extends StringHeader { 
  val name = "Expect" 
}

object From extends StringHeader { 
  val name = "From" 
}

object Host extends StringHeader { 
  val name = "Host" 
}

object IfMatch extends StringHeader { 
  val name = "If-Match" 
}

object IfModifiedSince extends StringHeader { 
  val name = "If-Modified-Since" 
}

object IfNoneMatch extends StringHeader { 
  val name = "If-None-Match" 
}

object IfRange extends StringHeader { 
  val name = "If-Range" 
}

object IfUnmodifiedSince extends StringHeader { 
  val name = "If-Unmodified-Since" 
}

object MaxForwards extends StringHeader { 
  val name = "Max-Forwards" 
}

object Pragma extends StringHeader { 
  val name = "Pragma" 
}

object ProxyAuthorization extends StringHeader { 
  val name = "Proxy-Authorization" 
}

object Range extends StringHeader { 
  val name = "Range" 
}

object Referer extends StringHeader { 
  val name = "Referer" 
}

object TE extends StringHeader { 
  val name = "TE" 
}

object Upgrade extends StringHeader { 
  val name = "Upgrade" 
}

object UserAgent extends StringHeader { 
  val name = "User-Agent" 
}

object Via extends StringHeader { 
  val name = "Via" 
}

object Warning extends StringHeader { 
  val name = "Warning" 
}
