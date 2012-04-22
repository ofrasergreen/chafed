package org.chafed

/**
 * A request body. 
 */
trait Body {
  /**
   * The content type (or `None`) if not specified. 
   */
  def contentType: Option[String] = None
  
  /**
   * The raw content of the body.
   */
  lazy val content: Array[Byte] = Array()
  
  /**
   * Extract additional headers we can derive given the content.
   */
  private[chafed] def implicitHeaders: List[Header] = contentType match {
    case Some(c) => 
      List(header.ContentType(c), header.ContentLength(content.size))
    case None => Nil
  }
}

/**
 * An empty request body.
 */
case object EmptyBody extends Body

/**
 * A URL-encoded request body.
 */
case class FormURLEncoded(query: Map[String, Option[String]]) extends Body with QueryBuilder {
  override lazy val content = optMap2Str(query).getBytes

  override def contentType = Some("application/x-www-form-urlencoded")
}

