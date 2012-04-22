package org.chafed
import java.io.BufferedReader
import scala.xml.NodeSeq

trait Method

case object Get extends Method
case object Head extends Method
case object Post extends Method
case object Put extends Method
case object Delete extends Method
case object Trace extends Method

case class URL(scheme: Scheme, host: String, port: Int, path: String, query: String) extends URI(null) {
  override def toString = {
    scheme.name + "://" + host + (if (port == scheme.defaultPort) "" else ":" + port) + path + (if (query.isEmpty) "" else "?" + query)
  }
  
  override def toURL(previous: URL) = this
    
  lazy val uri = new java.net.URI(scheme.name, "", host, scheme.defaultPort, path, query, "")
  lazy val url = uri.toURL
}

object URL {
  def apply(scheme: Scheme, host: String, path: String): URL = apply(scheme, host, scheme.defaultPort, path, "")
}


