package chafe.samples

import chafe._

object GoogleSearch {
  def main(args: Array[String]) {
    val ua = new UserAgent(logger = PrintfLogger)
  	val results = for {
  	  google <- ua.GET("http://www.google.com")
  	  resultPage <- google.submit("input[type=text]" -> "scala web scraping chafe")
  	  result <- resultPage.$(".r > a")
  	  page <- result.click if !(for {
  	    a <- page.$("a")
  	    href <- a.attribute("href") if href.text.contains("github.com/ofrasergreen/chafe")
  	  } yield 1).isEmpty
  	} yield page.resource
  	
  	println("Found:")
  	results.foreach(println(_))
  }
}