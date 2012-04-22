Chafe
=====

Chafe is a web scraping library for Scala. It provides a DSL for fetching web
pages, following links, extracting content, filling in forms and more.

Example
=======

```scala
import org.chafed._
for {
  githubProject <- UserAgent GET("https://github.com/ofrasergreen/chafed")
  treeBrowser <- githubProject $(".tree-browser")
  readmePage <- treeBrowser click("README.md")
  readme <- readmePage click$("#raw-url")
} println(readme)
```

This uses Scala's for-comprehension to compose a set of actions to:

1. Fetch Chafe's github project page.
1. Extract the HTML for the project tree browser by using a CSS selector to
   find a tag with the *tree-browser* class.
1. Follow the link containing the text "README.md".
1. Follow the link to the "RAW" content using a CSS selector to find a tag
   with the *raw-url* ID.
1. Print its content.

See [samples](chafed/tree/master/samples) for more examples.

Building and Installation
=========================

To use Chafe in your own [sbt]("https://github.com/harrah/xsbt") project, add
the following to your *build.sbt*:

```scala
libraryDependencies += "chafed" %% "chafe" % "0.2"
```

Use [sbt]("https://github.com/harrah/xsbt") to build from source:

```
$ sbt clean update package
```

The finished jar will be in *target*.


