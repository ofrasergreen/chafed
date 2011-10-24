package chafe
import java.net.HttpURLConnection
import java.io.{BufferedReader,InputStreamReader,Reader}
import scala.xml.NodeSeq
import collection.JavaConversions._
import java.io.DataOutputStream

class HttpURLConnectionHttp extends Client {
  def fetch(request: Request)(parse: (Int, String, List[Header], BufferedReader) => Response): Response = {
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
    val inputStreamReader = try {
      val re = """.*charset=([^()<>@,;:\"/\[\]?={}\s]*).*""".r
      val re(charSet) = connection.getContentType
      new InputStreamReader(inputStream, charSet)
    } catch {
      case t: Throwable => new InputStreamReader(inputStream)        
    }
    val reader = new BufferedReader(inputStreamReader)
    
    // Extract the headers
    val headers = (for {
      (name, values) <- connection.getHeaderFields()
      value <- values
    } yield {
      Header(name, value)
    }).toList.filter(_.name != null)
    
    // Invoke the parser on the data
    val response = parse(connection.getResponseCode, connection.getResponseMessage, headers, reader)
    
    // Close everything
    reader.close
    inputStreamReader.close
    inputStream.close
    connection.disconnect
    
    response
  }
}