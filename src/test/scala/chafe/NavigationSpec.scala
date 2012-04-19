package chafe

import org.specs2.mutable._
import scala.xml._
import URLBuilder._

object mockhttp extends Scheme("http", 80) {
  def invoke(request: Request): Response = 
    HtmlResponse(Text(request.method.toString.toUpperCase + " " + request.resource), Context(Request.Nil))
}

class NavigationSpec extends Specification {
  "The UserAgent" should {
    "have a default identity if none is provided" in {
      UserAgent.context.request.headers.find(_.name == "User-Agent") must_== Some(header.UserAgent("Chafe/1.0")) 
    }
  }
  
  "A URL" should {
    "be constructed from DSL including scheme, port, host, path and query" in {
      val url = mockhttp.:/("www.google.com", 81) / "foo/bar" ? "something"
      url.toString must_== "http://www.google.com:81/foo/bar?something"
    }
    
    "be constructed from DSL including scheme, host, path and query" in {
      val url = mockhttp :/ "www.google.com" / "foo/bar" ? "something"
      url.toString must_== "http://www.google.com/foo/bar?something"
    }
    
    "be constructed from DSL including scheme, host and path" in {
      val url = mockhttp :/ "www.google.com" / "foo/bar"
      url.toString must_== "http://www.google.com/foo/bar"
    }
    
    "be constructed from DSL including scheme and host" in {
      val url = mockhttp :/ "www.google.com"
      url.toString must_== "http://www.google.com/"
    }
    
    "be constructed from DSL including scheme and query" in {
      val url = mockhttp :/ "www.google.com" ? "something"
      url.toString must_== "http://www.google.com/?something"
    }
    
    "be constructed from DSL using a query map and be URL-encoded" in {
      val url = mockhttp :/ "www.google.com" ? Map("a" -> "foo baz", "b" -> "bar%$!")
      url.toString must_== "http://www.google.com/?a=foo+baz&b=bar%25%24%21"
    }
    
    "be constructed from DSL from a string" in {
      val uri: URI = "http://www.google.com/"
      uri.toURL(URL(http, "localhost", "/")).toString must_== "http://www.google.com/"
    }
    
    "be constructed using a relative path" in {
      val uri: URI = "baz"
      uri.toURL(URL(http, "www.google.com", "/foo/bar")).toString must_== "http://www.google.com/foo/baz"
    }
    
    "be constructed using an absolute path" in {
      val uri: URI = "/foo"
      uri.toURL(URL(http, "www.google.com", "/")).toString must_== "http://www.google.com/foo"
    }
    
    "be constructed from DSL including path and query" in {
      val uri: URI = "foo" ? "something"
      uri.toURL(URL(http, "www.google.com", "/")).toString must_== "http://www.google.com/foo?something"
    }
    
    "be constructed from DSL using path parts" in {
      val uri: URI = "foo" / "bar"
      uri.toURL(URL(http, "www.google.com", "/")).toString must_== "http://www.google.com/foo/bar"
    }
  }
  
  "A page" should {
    "let you navigate to a new page" in {
      UserAgent.GET(mockhttp :/ "www.google.com" / "foo").get.content must_== Text("GET http://www.google.com/foo")
    }
    
    "use the previous page when navigating relatively" in {
      val page1 = UserAgent.GET(mockhttp :/ "www.google.com" / "foo/bar") 
      val page2 = page1.GET("baz")
      page2.get.content must_== Text("GET http://www.google.com/foo/baz")
    }
  }
}