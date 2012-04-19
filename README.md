Chafe
=====

Chafe is a web scraping library for Scala. It provides a DSL for fetching web
pages, following links, extracting content, filling in forms and more.

Example
=======

	import chafe._
	for {
	  githubProject <- UserAgent GET("https://github.com/ofrasergreen/chafe")
	  treeBrowser <- githubProject $(".tree-browser")
	  readmePage <- treeBrowser click("README.md")
	  readmeRawLink <- readmePage $("#raw-url")
	  readme <- readmeRawLink click
	} println(readme)

This uses Scala's for-comprehension to compose a set of actions to:

1. fetch Chafe's github project page.
1. extract the project tree browser by using a CSS selector to find a tag with
   the *tree-browser* class.
1. click on the link containing the text "README.md".
1. extract the link to the "RAW" content using a CSS selector to find a tag
   with the *raw-url* ID.
1. click the link and print its content.

See [samples](chafe/samples) for more examples.

Usage
=====

