package chafe.samples

import chafe._

/**
 * Navigate to chafe's own project README.md file and display it.
 */
object ChafeReadme {
  def main(args: Array[String]): Unit = {
    val ua = new UserAgent(logger = PrintfLogger)
    
    for {
      githubProject <- ua GET("https://github.com/ofrasergreen/chafe")
      treeBrowser <- githubProject $(".tree-browser")
      readmePage <- treeBrowser click("README.md")
      readme <- readmePage click$("#raw-url")
    } println(readme)
  }
}