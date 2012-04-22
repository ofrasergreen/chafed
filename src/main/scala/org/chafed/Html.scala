package org.chafed
import scala.xml.{ Node, Elem, PrettyPrinter }
import se.fishtank.css.selectors.Selectors

/**
  * An input supplied to a form
  */
case class Input(selector: String, value: String)

object Input {
  implicit def strStr2Input(s: (String, String)) = Input(s._1, s._2)
}

/**
 * Some HTML content. This maybe an entire page or just an excerpt after
 * running a `Selector`.
 */
class Html(
  val content: Node,
  private[chafed] val context: Context)(implicit val logger: Logger) {

  private lazy val ua = new UserAgent(context, logger)
  
  def resource = context.request.resource

  /**
   * Return the subset of this `Html` selected by the CSS selector. This may be zero
   * if there were no matches, one or multiple excerpts.
   */
  def $(selector: String): Seq[Html] = {
    content match {
      case elem: Elem => 
        val query = Selectors.query(selector, elem)
        query match {
          case Right(nodes) => nodes.map(new Html(_, context))
          case Left(msg) => throw new IllegalArgumentException(msg)
        }
      case _ => Nil
    }
  }

  /**
   * Submit all forms using `Input`s as values.
   */
  def submit(inputs: Input*): Response = {
    // Find every <form> element and submit it.
    $("form") match {
      case form :: _ => // Act on the first form
        // Turn the inputs into a Map so we can spot if we're hitting them when we iterate through
        // them later.
        val data = (for {
          Input(s, value) <- inputs
          input <- form.$(s)
          name <- input.attribute("name").map(_.text)
        } yield (name, value)).toMap
          
        val ins: Map[String, Option[String]] = (for {
          input <- (form.content \\ "input")
          t <- input.attribute("type")
          name <- input.attribute("name") if (input.attribute("disabled").isEmpty)
        } yield {
          val key = name.text
          val value: Option[String] = data.get(name.text) orElse input.attribute("value").map(_.text)
          t.text.toLowerCase match {
            case "text" | "password" | "hidden" | "submit" => key -> value
            case "checkbox" if (input.attribute("checked").isDefined) => key -> None
            case "radio" if (input.attribute("checked").isDefined) => key -> value
            case _ => "" -> None
          }
        }).filter(_ != ("", None)).toMap
  
        val action = form.content.flatMap(_.attribute("action").map(_.text)).headOption.getOrElse("")
        form.content.flatMap(_.attribute("method").map(_.text.toLowerCase)).headOption match {
          case Some("get") =>
            val queryBuilder = new Object with QueryBuilder
            val query = queryBuilder.optMap2Str(ins)
            ua.GET(URI(if (action.contains("?")) (action + "&" + query) else (action + "?" + query)))
          case _ => ua.POST(URI(action), ins)
        }
      case _ => // If there wasn't a form
        InvalidRequest("No form found to submit.", context)
    }
  }

  /**
   * Click and follow all links
   */
  def click(): Response = {
    (for {
      a <- $("a")
      link <- a.attribute("href")
    } yield {
      ua.GET(link.text)
    }).headOption.getOrElse(InvalidRequest("No valid <a> tag found to click.", context))
  }

  def click$(selector: String): Response = {
    $(selector).map(_.click).headOption.getOrElse(InvalidRequest("No valid <a> tag found to click.", context))
  }
  
  /**
   * Click and follow all links containing `text`.
   */
  def click(text: String): Response = {
    (for {
      as <- $("a")
      a <- as.content if (a.text.trim == text)
      link <- a.attribute("href")
    } yield {
      ua.GET(link.text)
    }).headOption.getOrElse(InvalidRequest("No <a> tag found countaining '" + text + "'to click.", context))
  }

  /**
   * Return the text content, trimmed.
   */
  def text(): String = {
    val whitespace = """[\s ]+""".r
    whitespace.replaceAllIn(content.text.trim, " ")
  }

  override def toString = {
    val printer = new PrettyPrinter(80, 2)
    printer.format(content)
  }
}

object Html {
  // Pimp `scala.xml.Node` such that `Node`and `Html` are interchangeable 
  implicit def node2Html(n: Node): Html = new Html(n, UserAgent.context)(UserAgent.logger)
  implicit def html2Node(h: Html): Node = h.content
}

//class HtmlSeq(val nodes: Seq[Html]) {
//  def text(): String = nodes.map(_.text).mkString
//  def $(selector: Selector): HtmlSeq = nodes.flatMap(_.$(selector))
//}
//
//object HtmlSeq {
//  implicit def seqHtml2HtmlSeq(s: Seq[Html]): HtmlSeq = new HtmlSeq(s)
//  implicit def htmlSeq2SeqHtml(htmlSeq: HtmlSeq): Seq[Html] = htmlSeq.nodes
//}
