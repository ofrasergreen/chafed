package chafe

import scala.util.parsing.combinator.{Parsers, ImplicitConversions}
import scala.xml.NodeSeq

sealed trait Selector {
  def subNodes: Option[SubNode]
}

object Selector {
  implicit def str2Selector(str: String) = 
    SelectorParser.parse(str).getOrElse(throw new Exception("Couldn't parse selector expression: " + str))
}

final case class ElemSelector(elem: String, subNodes: Option[SubNode]) extends 
  Selector

final case class StarSelector(subNodes: Option[SubNode]) extends Selector

final case class IdSelector(id: String, subNodes: Option[SubNode]) extends 
  Selector

final case class ClassSelector(clss: String, subNodes: Option[SubNode]) extends
  Selector

final case class NameSelector(name: String, subNodes: Option[SubNode]) extends
  Selector

final case class AttrSelector(elem: String, name: String, value: String, 
subNodes: Option[SubNode]) extends Selector

sealed trait SubNode

object SubNode {
//  def unapply(bind: CssBind): Option[Option[SubNode]] = 
//    Some(bind.css.flatMap(_.subNodes))
}

sealed trait WithKids {
  def transform(original: NodeSeq, newNs: NodeSeq): NodeSeq
}

final case class KidsSubNode() extends SubNode with WithKids {
  def transform(original: NodeSeq, newNs: NodeSeq): NodeSeq = newNs
}

final case class PrependKidsSubNode() extends SubNode with WithKids {
  def transform(original: NodeSeq, newNs: NodeSeq): NodeSeq = newNs ++ original
}

final case class AppendKidsSubNode() extends SubNode with WithKids {
  def transform(original: NodeSeq, newNs: NodeSeq): NodeSeq = original ++ newNs
}

sealed trait AttributeRule

final case class AttrSubNode(attr: String) extends SubNode with AttributeRule
final case class AttrAppendSubNode(attr: String) extends SubNode with AttributeRule
final case class AttrRemoveSubNode(attr: String) extends SubNode with AttributeRule

final case class SelectThisNode(kids: Boolean) extends SubNode

final case class SelectorSubNode(selector: Selector) extends SubNode

/**
 * Parse a subset of CSS into the appropriate selector objects
 */
object SelectorParser extends Parsers with ImplicitConversions {
  private val cache = scala.collection.mutable.Map[String, Selector]()

  /**
   * Parse a String into a CSS Selector
   */
  def parse(_toParse: String): Option[Selector] = synchronized {
    // trim off leading and trailing spaces
    val toParse = _toParse.trim

    // this method is synchronized because the Parser combinator is not
    // thread safe, so we'll only parse one at a time, but given that most
    // of the selectors will be cached, it's not really a performance hit
    cache.get(toParse) orElse {
      internalParse(toParse).map {
        sel => {
          // cache the result
          cache(toParse) = sel
          sel
        }
      }
    }
  }

  import scala.util.parsing.input.CharSequenceReader
  type Elem = Char

  type UnitParser=Parser[Unit]

  private def internalParse(toParse: String): Option[Selector] = {
    val reader: Input = new CharSequenceReader(toParse, 0)
    topParser(reader) match {
      case Success(v, _) => Some(v)
      case x => None
    }
  }

  private implicit def str2chars(s: String): List[Char] = new scala.collection.immutable.WrappedString(s).toList

  private lazy val topParser: Parser[Selector] = {
    phrase(idMatch |
           nameMatch |
           classMatch |
           attrMatch |
           elemMatch |
           starMatch)
  }
    
//  private lazy val colonMatch: Parser[Sele  ctor] =
//    ':' ~> id ~ opt(subNode) ^? {
//      case "button" ~ sn => AttrSelector("*", "type", "button", sn)
//      case "checkbox" ~ sn => AttrSelector("type", "checkbox", sn)
//      case "file" ~ sn => AttrSelector("type", "file", sn)
//      case "password" ~ sn => AttrSelector("type", "password", sn)
//      case "radio" ~ sn => AttrSelector("type", "radio", sn)
//      case "reset" ~ sn => AttrSelector("type", "reset", sn)
//      case "submit" ~ sn => AttrSelector("type", "submit", sn)
//      case "text" ~ sn => AttrSelector("type", "text", sn)
//    }

  private lazy val idMatch: Parser[Selector] = '#' ~> id ~ opt(subNode) ^^ {
    case id ~ sn => IdSelector(id, sn)
  }

  private lazy val nameMatch: Parser[Selector] = '@' ~> id ~ opt(subNode) ^^ {
    case name ~ sn => NameSelector(name, sn)
  }

  private lazy val elemMatch: Parser[Selector] =  id ~ opt(subNode) ^^ {
    case elem ~ sn => ElemSelector(elem, sn)
  }

  private lazy val starMatch: Parser[Selector] =  '*' ~> opt(subNode) ^^ {
    case sn => StarSelector(sn)
  }


  private lazy val id: Parser[String] = letter ~ 
  rep(letter | number | '-' | '_' | ':' | '.') ^^ {
    case first ~ rest => (first :: rest).mkString
  }

  private def isLetter(c: Char): Boolean = c.isLetter

  private def isNumber(c: Char): Boolean = c.isDigit

    
  private lazy val letter: Parser[Char] = elem("letter", isLetter)
  private lazy val number: Parser[Char] = elem("number", isNumber)

    private lazy val classMatch: Parser[Selector] = 
    '.' ~> attrName ~ opt(subNode) ^^ {
      case cls ~ sn => ClassSelector(cls, sn)
    }

  private lazy val attrMatch: Parser[Selector] = 
    '[' ~ attrName ~ '=' ~ attrConst ~ opt(subNode) ~ ']' ^^ {
      case _ ~ "id" ~ _ ~ const ~ sn ~ _ => IdSelector(const, sn)
      case _ ~ "name" ~ _ ~ const ~ sn ~ _ => NameSelector(const, sn)
      case _ ~ n ~ _  ~ v ~ sn ~ _ => AttrSelector("*", n, v, sn)
    }

  private lazy val subNode: Parser[SubNode] = rep1(' ') ~>
      topParser ^^ {
        case selector => SelectorSubNode(selector)
      }
  
//  ((opt('*') ~ '[' ~> attrName <~ '+' ~ ']' ^^ {
//    name => AttrAppendSubNode(name)
//  }) | 
//  (opt('*') ~ '[' ~> attrName <~ '!' ~ ']' ^^ {
//    name => AttrRemoveSubNode(name)
//  }) |    (opt('*') ~ '[' ~> attrName <~ ']' ^^ {
//     name => AttrSubNode(name)
//   }) | 
//   ('-' ~ '*' ^^ (a => PrependKidsSubNode())) |
//   ('>' ~ '*' ^^ (a => PrependKidsSubNode())) |
//   ('*' ~ '+' ^^ (a => AppendKidsSubNode())) |
//   ('*' ~ '<' ^^ (a => AppendKidsSubNode())) |
//   '*' ^^ (a => KidsSubNode()) |
//   '^' ~ '*' ^^ (a => SelectThisNode(true)) |
//   '^' ~ '^' ^^ (a => SelectThisNode(false)))

  private lazy val attrName: Parser[String] = (letter | '_' | ':') ~
  rep(letter | number | '-' | '_' | ':' | '.') ^^ {
    case first ~ rest => (first :: rest).mkString
  }
  private lazy val attrConst: Parser[String] = {
    (('\'' ~> rep(elem("isValid", (c: Char) => {
      c != '\'' && c >= ' '
    })) <~ '\'') ^^ {
      case s => s.mkString
    }) |
    (('"' ~> rep(elem("isValid", (c: Char) => {
      c != '"' && c >= ' '
    })) <~ '"') ^^ {
      case s => s.mkString
    }) |
    (rep1(elem("isValid", (c: Char) => {
      c != '\'' && c != '"' && c > ' '
    })) ^^ {
      case s => s.mkString
    })
    
  }

  
}
