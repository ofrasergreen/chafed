package chafe

object URLBuilder {
  implicit def str2PathParts(path: String) = new PathPartsBuilder(path)
  implicit def str2QueryParts(path: String) = new QueryPartsBuilder(path)
  //implicit def str2URI(uri: String) = new URL(previous.scheme, previous.host, "", "")
  //}
}

class HostURL(scheme: Scheme, host: String, port: Int) extends URL(scheme, host, port, "/", "") {
  def this(scheme: Scheme, host: String) = this(scheme, host, scheme.defaultPort)
  
  def /(path: String) = new PathURL(scheme, host, scheme.defaultPort, "/" + path)
  def /(queryParts: QueryParts) = new QueryURL(scheme, host, port, "/" + queryParts.path.path, queryParts.query)
}

case class PathParts(path: String)

class PathPartsBuilder(path: String) {
  def /(tail: String) = PathParts(path + "/" + tail)
  def /(queryParts: QueryParts) = queryParts.copy(path=PathParts(path + "/" + queryParts.path.path))
}

trait QueryBuilder {
  import java.net.URLEncoder
  
  implicit def optMap2Str(query: Map[String, Option[String]]) = query.map { 
    case (k, vOpt) => URLEncoder.encode(k,  "UTF-8") + (vOpt.map(v => "=" + URLEncoder.encode(v, "UTF-8")).getOrElse(""))
  }.mkString("&")
  
  implicit def map2str(query: Map[String, String]) = optMap2Str(query.map({case (k, v) => (k, Some(v))}))
}

case class QueryParts(path: PathParts, query: String)
class QueryPartsBuilder(path: String) extends QueryBuilder {
  def ?(query: String) = QueryParts(PathParts(path), query)
  def ?(map: Map[String, String]) = QueryParts(PathParts(path), map)
}

class PathURL(scheme: Scheme, host: String, port: Int, path: String) extends URL(scheme, host, port, path, "") with QueryBuilder {
  def ?(query: String) = new QueryURL(scheme, host, port, path, query)
  def ?(map: Map[String, String]) = new QueryURL(scheme, host, port, path, map)
}

class QueryURL(scheme: Scheme, host: String, port: Int, path: String, query: String) extends URL(scheme, host, port, path, query)

