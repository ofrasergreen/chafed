package chafe

import org.scalatest.FlatSpec
import scala.xml._
import URLBuilder._

object mockhttp extends Scheme("http", 80) {
  def invoke(request: Request): Response = 
    HtmlResponse(Text(request.method.toString.toUpperCase + " " + request.resource), Context(Request.Nil))(NullLogger)
}

class NavigationSpec extends FlatSpec {
  "The UserAgent" should "have a default identity if none is provided" in {
    assert(UserAgent.context.request.headers.find(_.name == "User-Agent") === Some(header.UserAgent("Chafe/1.0")))
  }
  
  "A URL" should "be constructed from DSL including scheme, port, host, path and query" in {
    val url = mockhttp.:/("www.google.com", 81) / "foo/bar" ? "something"
    assert(url.toString === "http://www.google.com:81/foo/bar?something")
  }
  
  it should "be constructed from DSL including scheme, host, path and query" in {
    val url = mockhttp :/ "www.google.com" / "foo/bar" ? "something"
    assert(url.toString === "http://www.google.com/foo/bar?something")
  }
  
  it should "be constructed from DSL including scheme, host and path" in {
    val url = mockhttp :/ "www.google.com" / "foo/bar"
    assert(url.toString === "http://www.google.com/foo/bar")
  }
  
  it should "be constructed from DSL including scheme and host" in {
    val url = mockhttp :/ "www.google.com"
    assert(url.toString === "http://www.google.com/")
  }
  
  it should "be constructed from DSL including scheme and query" in {
    val url = mockhttp :/ "www.google.com" ? "something"
    assert(url.toString === "http://www.google.com/?something")
  }
  
  it should "be constructed from DSL using a query map and be URL-encoded" in {
    val url = mockhttp :/ "www.google.com" ? Map("a" -> "foo baz", "b" -> "bar%$!")
    assert(url.toString === "http://www.google.com/?a=foo+baz&b=bar%25%24%21")
  }
  
  it should "be constructed from DSL from a string" in {
    val uri: URI = "http://www.google.com/"
    assert(uri.toURL(URL(http, "localhost", "/")).toString === "http://www.google.com/")
  }
  
  it should "be constructed using a relative path" in {
    val uri: URI = "baz"
    assert(uri.toURL(URL(http, "www.google.com", "/foo/bar")).toString === "http://www.google.com/foo/baz")
  }
  
  it should "be constructed using an absolute path" in {
    val uri: URI = "/foo"
    assert(uri.toURL(URL(http, "www.google.com", "/")).toString === "http://www.google.com/foo")
  }
  
  it should "be constructed from DSL including path and query" in {
    val uri: URI = "foo" ? "something"
    assert(uri.toURL(URL(http, "www.google.com", "/")).toString === "http://www.google.com/foo?something")
  }
  
  it should "be constructed from DSL using path parts" in {
    val uri: URI = "foo" / "bar"
    assert(uri.toURL(URL(http, "www.google.com", "/")).toString === "http://www.google.com/foo/bar")
  }
  
  "A page" should "let you navigate to a new page" in {
    // TODO
    //assert(UserAgent.GET(mockhttp :/ "www.google.com" / "foo").get.content === Text("GET http://www.google.com/foo"))
  }
    
  it should "use the previous page when navigating relatively" in {
    //val page1 = UserAgent.GET(mockhttp :/ "www.google.com" / "foo/bar") 
    //val page2 = page1.GET("baz")
    //assert(page2.get.content === Text("GET http://www.google.com/foo/baz"))
  }
}