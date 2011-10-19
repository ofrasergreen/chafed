package chafe

trait Body {
  def contentType: Option[String] = None
  lazy val content: Array[Byte] = Array()
  
  def headers: List[Header] = contentType match {
    case Some(c) => 
      List(Headers.ContentType(c), Headers.ContentLength(content.size))
    case None => Nil
  }
}

case object EmptyBody extends Body

case class FormURLEncoded(query: Map[String, Option[String]]) extends Body with QueryBuilder {
  override lazy val content = optMap2Str(query).getBytes

  override def contentType = Some("application/x-www-form-urlencoded")
}

