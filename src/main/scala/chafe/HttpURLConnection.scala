package chafe
import java.net.HttpURLConnection
import java.io.{BufferedReader,InputStreamReader,Reader, InputStream}
import scala.xml.NodeSeq
import collection.JavaConversions._
import java.io.DataOutputStream

class HttpURLConnectionHttp extends Client {
  def fetch(request: Request)(parse: (Int, String, List[Header], InputStream) => Response): Response = {
    val connection = request.resource.url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setInstanceFollowRedirects(false)
    connection.setDoInput(true)
    connection.setDoOutput(true)
    connection.setRequestMethod(request.method.toString.toUpperCase)
    request.headers.foreach(h => connection.setRequestProperty(h.name, h.value))
    
    // Build the request
    request.body match {
      case EmptyBody => // Do nothing
      case body =>
        val outputStream = new DataOutputStream(connection.getOutputStream)
        outputStream.write(body.content)
        outputStream.flush
        outputStream.close
    }
    
    // Get response
    val inputStream = try {
      connection.getInputStream
    } catch {
    case e: java.net.ConnectException =>
      throw new ClientException("Error fetching '" + request.resource + "': " + e.getMessage)
    }
    
    // Extract the headers
    val headers = (for {
      (name, values) <- connection.getHeaderFields()
      value <- values
    } yield {
      Header(name, value)
    }).toList.filter(_.name != null)
    
    // Invoke the parser on the data
    val response = parse(connection.getResponseCode, connection.getResponseMessage, headers, inputStream)
    
    // Close everything
    inputStream.close
    connection.disconnect
    
    response
  }
}