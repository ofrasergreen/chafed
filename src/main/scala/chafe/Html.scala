package chafe
import scala.xml.{NodeSeq, Node}

trait Selectable {
  protected val content: NodeSeq
  protected val context: Context
  
  private def hasClass(value: String)(node: Node) = 
    node.attribute("class").map(_.text).exists(_.split(" ").contains(value))
    
  private def hasAttr(attr: String, value: String)(node: Node) = 
    node.attribute(attr).map(_.text).exists(_ == value)
    
  private def select(selector: Selector): NodeSeq = {
    val selected: NodeSeq = selector match {
      case ClassSelector(clss, subNodes) => (content \\ "_" filter hasClass(clss))
      case ElemSelector(elem, subNodes) => (content \\ elem)
      case IdSelector(id, subNodes) => (content \\ "_" filter hasAttr("id", id))
      case NameSelector(name, subNodes) => (content \\ "_" filter hasAttr("name", name))
      case _ => 
        println("TODO: select " + selector)
        Nil
    }
    
    selector.subNodes match {
      case Some(SelectorSubNode(s)) => select(s)
      case None => selected
      case Some(s) =>
        println("TODO: match " + s)
        selected
    }
  }
    
  def $(selector: Selector): HtmlSeq = select(selector).map(new Html(_, context))
  
  def submit(selector: Selector, inputs: Input*): Seq[Response] = {
    $(selector).flatMap { form =>
      // Turn the inputs into a Map so we can spot if we're hitting them when we iterate through
      // them later.
      val data = inputs.flatMap({case Input(s, value) => form.select(s).map((_, value))}).toMap 
      
      val ins: Map[String, Option[String]] = (for {
        input <- (form.content \\ "input")
        t <- input.attribute("type")
        name <- input.attribute("name") if (input.attribute("disabled").isEmpty)
      } yield {
        val key = name.text
        val value: Option[String] = data.get(input) orElse input.attribute("value").map(_.text)
        t.text.toLowerCase match {
          case "text" | "password" | "hidden" | "submit" => key -> value
          case "checkbox" if (input.attribute("checked").isDefined) => key -> None
          case "radio" if (input.attribute("checked").isDefined) => key -> value
          case _ => "" -> None
        } 
      }).filter(_ != ("", None)).toMap
      
      
      //println(ins)
      val action = form.content.flatMap(_.attribute("action").map(_.text)).headOption.getOrElse("")
      List(context.invoke(Request(Post, URI(action).toURL(context.request.resource), body=FormURLEncoded(ins))))
    }
  }
  
  def click(): Seq[Response] = {
    for {
      a <- select(ElemSelector("a", None))
      link <- a.attribute("href")
    } yield {
      context.invoke(Request(Get, URI(link.text).toURL(context.request.resource), Nil))
    }
  }
  
  def click(selector: Selector): Seq[Response] = {
    for {
      a <- select(selector) if (a.label.toLowerCase == "a")
      link <- a.attribute("href")
    } yield {
      context.invoke(Request(Get, URI(link.text).toURL(context.request.resource), Nil))
    }
  }
  
  def clickText(text: String): Seq[Response] = {
    for {
      as <- $(ElemSelector("a", None))
      a <- as.content if (a.text.trim == text)
      link <- a.attribute("href")
    } yield {
      context.invoke(Request(Get, URI(link.text).toURL(context.request.resource), Nil))
    }
  }
  
  def text(): String = {
    val whitespace = """[\s ]+""".r
    whitespace.replaceAllIn(content.text.trim, " ")
  }
}

class HtmlSeq(val nodes: Seq[Html]) {
  def text(): String = nodes.map(_.text).mkString
  def $(selector: Selector): HtmlSeq = nodes.flatMap(_.$(selector))
}

object HtmlSeq {
  implicit def seqHtml2HtmlSeq(s: Seq[Html]): HtmlSeq = new HtmlSeq(s)
  implicit def htmlSeq2SeqHtml(htmlSeq: HtmlSeq): Seq[Html] = htmlSeq.nodes
}

case class Html(content: NodeSeq, context: Context) extends Selectable
