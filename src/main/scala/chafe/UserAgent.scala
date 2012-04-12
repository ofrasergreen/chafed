package chafe

class UserAgent(identity: String) extends NilResponse(header.UserAgent(identity) ::
    header.Accept("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") :: Nil) {
}

object UserAgent extends UserAgent("Chafe/1.0") {
  def apply(identity: String): Response = new UserAgent(identity)
  
  object Chrome extends UserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1")
  object Safari extends UserAgent("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/312.8 (KHTML, like Gecko) Safari/312.3.32011-10-10 14:01:24")
  object Firefox extends UserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.23) Gecko/20110920 Firefox/3.6.232011-10-10 14:01:11")
  object IE9 extends UserAgent("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 7.1; Trident/5.0)")
  object iPad extends UserAgent("Mozilla/5.0 (iPad; U; CPU OS 4_3_5 like Mac OS X; sv-se) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.52011-10-10 14:01:20")
  object Android extends UserAgent("Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1")
}