package org.chafed
import java.io.BufferedReader

trait Client {
  def fetch(request: Request)(parse: (Int, String, List[Header], BufferedReader) => Response): Response
}

object Client {
  def apply(scheme: Scheme) = scheme match {
    case org.chafed.http | org.chafed.https => new HttpURLConnectionHttp()
  }
}

case class ClientException(reason: String) extends Exception(reason)

