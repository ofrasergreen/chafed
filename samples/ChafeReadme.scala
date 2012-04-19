package chafe.samples

import chafe._

object ChafeReadme {
  def main(args: Array[String]): Unit = {
    val ua = new UserAgent(logger = PrintfLogger)
    
    for {
      githubProject <- UserAgent.GET("https://github.com/ofrasergreen/chafe")
      treeBrowser <- githubProject.$(".tree-browser")
      readmePage <- treeBrowser.click("README.md")
      readmeRawLink <- readmePage.$("#raw-url")
      readme <- readmeRawLink.click
    } println(readme)
  }
}