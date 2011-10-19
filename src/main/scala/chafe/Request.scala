package chafe

case class Request(
    method: Method,
    resource: URL,
    headers: List[Header] = List(),
    body: Body = EmptyBody
) {
  def merge(previous: Request, cookies: List[Cookie]): Request = {
    def cookieHeader = Headers.Cookie(cookies.map({ cookie =>
      cookie.name match {
        case Some(name) => name + "=" + cookie.value
        case _ => cookie.value
      }
    }).mkString("; "))
    
    def refererHeader = Headers.Referer(previous.resource.toString)
  
    copy(headers = refererHeader :: cookieHeader :: headers ++ body.headers ++ previous.headers.filter(h => (!headers.exists(_.name == h.name) && !(h.name == "Cookie"))))
  }
}

object Request {
  object Nil extends Request(Get, URL(http, "localhost", "/"))
}
