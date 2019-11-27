package ch.epfl.bluebrain.nexus.sbt.nexus

import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{dockerChmodType, Docker}
import com.typesafe.sbt.packager.docker.{DockerChmodType, DockerPlugin, DockerVersion}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import sbt.Keys._
import sbt._

/**
  * Plugin defining default configuration for building docker images for akka based service implementations.
  */
object ServicePackagingPlugin extends AutoPlugin {

  override lazy val requires = UniversalPlugin && JavaAppPackaging && DockerPlugin

  override lazy val trigger = noTrigger

  val downloadWaitForItScript = taskKey[File]("Downloads the wait-for-it.sh script to the target folder")

  override lazy val projectSettings = Seq(
    downloadWaitForItScript := {
      import scala.sys.process._
      val waitUrl = url("https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh")
      val file    = target.value / "wait-for-it.sh"
      assert((waitUrl #> file !) == 0, "Downloading wait-for-it.sh script failed")
      file
    },
    // package the kanela agent as a fixed name jar
    mappings in Universal := {
      val universalMappings = (mappings in Universal).value
      universalMappings.foldLeft(Vector.empty[(File, String)]) {
        case (acc, (file, filename)) if filename.contains("kanela-agent") =>
          acc :+ (file -> "lib/instrumentation-agent.jar")
        case (acc, other) =>
          acc :+ other
      } :+ (downloadWaitForItScript.value -> "bin/wait-for-it.sh")
    },
    // docker publishing settings
    Docker / maintainer := "Nexus Team <noreply@epfl.ch>",
    Docker / version    := "latest",
    Docker / daemonUser := "nexus",
    dockerBaseImage     := "adoptopenjdk:11-jre-hotspot",
    dockerExposedPorts  := Seq(8080, 2552),
    dockerUsername      := Some("bluebrain"),
    dockerUpdateLatest  := false,
    dockerChmodType     := DockerChmodType.UserGroupWriteExecute,
    dockerVersion       := Some(DockerVersion(19, 3, 5, Some("ce"))) // forces the version because gh-actions version is 3.0.x which is not recognized to support multistage
  )
}
