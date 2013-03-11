organization := "org.chafed"

name := "chafed"

version := "0.2.2"

scalaVersion := "2.10.0"

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "se.fishtank" %% "css-selectors-scala" % "0.1.3"
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "UTF-8")

// ---------------------------------------------------------------------------
// Publishing criteria

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://chafe.org/</url>
  <scm>
    <url>git@github.com:ofrasergreen/chafed.git/</url>
    <connection>scm:git:git@github.com:ofrasergreen/chafed.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ofrasergreen</id>
      <name>Owen Fraser-Green</name>
      <url>http://www.fraser-green.com</url>
    </developer>
  </developers>
)
