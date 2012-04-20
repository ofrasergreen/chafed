organization := "chafe"

name := "chafe"

version := "0.2"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.7.1" % "test",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "se.fishtank" %% "css-selectors-scala" % "0.1.1"
)

scalacOptions ++= Seq("-deprecation")
