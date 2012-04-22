package org.chafed.samples

import org.chafed._

/**
 * Count the number of pages in the first page of results from search
 * Google for: scala web scraping
 */
object GoogleSearch {
  def main(args: Array[String]) {
    val ua = new UserAgent(logger = PrintfLogger)
  	val results = for {
  	  google <- ua GET("http://www.google.com")
  	  results <- google submit("input[type=text]" -> "scala web scraping")
  	  result <- results click$(".r > a")
  	  if !(for {
  	    a <- result $("a")
  	    href <- a.attribute("href") if href.text.contains("github.com/ofrasergreen/chafed")
  	  } yield 1).isEmpty
  	} yield result.resource
  	
  	println("Found:")
  	results.foreach(println(_))
  }
}