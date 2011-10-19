package chafe

case class Input(selector: Selector, value: String)

object Input {
  implicit def strStr2Input(s: (String, String)) = Input(s._1, s._2)
  implicit def selectorStr2Input(s: (Selector, String)) = Input(s._1, s._2) 
}