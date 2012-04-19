organization := "chafe"

name := "chafe"

version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.6.1" % "test",
  "org.specs2" %% "specs2-scalaz-core" % "6.0.RC2" % "test",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "se.fishtank" %% "css-selectors-scala" % "0.1.1"
)

scalacOptions ++= Seq("-deprecation")
