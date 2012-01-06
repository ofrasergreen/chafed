package chafe

abstract class Scheme(val name: String, val defaultPort: Int) {
  def :/(host: String) = new HostURL(this, host)
  def :/(host: String, port: Int) = new HostURL(this, host, port)
  def :/(pathParts: PathParts): PathURL = {
    pathParts.path.split("/").toList match {
      case host :: path => new PathURL(this, host, defaultPort, "/" + path.mkString("/"))
      case Nil => new PathURL(this, "localhost", defaultPort, "")
    }
  }
  def :/(queryParts: QueryParts): QueryURL = {
    :/(queryParts.path) ? queryParts.query
  }
}

object Scheme {
  def all = http :: https :: Nil
}

case object http extends Scheme("http", 80)

case object https extends Scheme("https", 443)
