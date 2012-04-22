import sbt._
import Keys._

object ChafeBuild extends Build {
    lazy val chafed = Project(id = "chafed",
                            base = file("."))

    lazy val samples = Project(id = "chafed-samples",
                           base = file("samples")) dependsOn(chafed)
}
