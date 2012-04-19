package chafe

case class Request(
    method: Method,
    resource: URL,
    headers: List[Header] = List(),
    body: Body = EmptyBody
) {
  def merge(previous: Request, cookies: List[Cookie]): Request = {
    def cookieHeader = header.Cookie(cookies.map({ cookie =>
      cookie.name match {
        case Some(name) => name + "=" + cookie.value
        case _ => cookie.value
      }
    }).mkString("; "))
    
    def refererHeader = header.Referer(previous.resource.toString)
  
    copy(headers = refererHeader :: cookieHeader :: headers ++ body.implicitHeaders ++ previous.headers.filter(h => (!headers.exists(_.name == h.name) && !(h.name == "Cookie") && !(h.name == "Referer"))))
  }
  
  override def toString() = {
    "> " + method.toString.toUpperCase + " " + resource + "\n" + 
        headers.map("> " + _.toString).mkString("\n") + "\n"
  }
}

object Request {
  object Nil extends Request(Get, URL(http, "localhost", "/"))
}
