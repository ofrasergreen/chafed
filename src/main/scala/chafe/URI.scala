package chafe

class URI(uri: java.net.URI) {
  def toURL(previous: URL) = {
    val u = previous.uri.resolve(uri)
    val scheme = URLBuilder.str2Scheme(u.getScheme())
    val query = if (u.getQuery == null) "" else u.getQuery
    val port = if (u.getPort == -1) scheme.defaultPort else u.getPort
    URL(scheme, u.getHost, port, u.getPath, query)
  }
}

object URI {
  def apply(s: String) = new URI(new java.net.URI(s))
  implicit def str2URI(s: String) = apply(s)
  implicit def queryParts2URI(qp: QueryParts) = new URI(new java.net.URI(qp.path.path + "?" + qp.query))
  implicit def pathParts2URI(pp: PathParts) = new URI(new java.net.URI(pp.path))
}