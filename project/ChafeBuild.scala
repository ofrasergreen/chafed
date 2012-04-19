import sbt._
import Keys._

object ChafeBuild extends Build {
    lazy val chafe = Project(id = "chafe",
                            base = file("."))

    lazy val samples = Project(id = "chafe-samples",
                           base = file("samples")) dependsOn(chafe)
}