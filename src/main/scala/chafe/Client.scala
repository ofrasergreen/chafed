package chafe
import java.io.InputStream

trait Client {
  def fetch(request: Request)(parse: (Int, String, List[Header], InputStream) => Response): Response
}

object Client {
  def apply(scheme: Scheme) = scheme match {
    case chafe.http | chafe.https => new HttpURLConnectionHttp()
  }
}

case class ClientException(reason: String) extends Exception(reason)

