package ch.epfl.bluebrain.nexus.sbt.nexus

import java.util.regex.Pattern

import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{Docker, DockerAlias}
import com.typesafe.sbt.packager.docker.{Cmd, DockerPlugin, ExecCmd}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import sbt.Keys._
import sbt._

/**
  * Plugin defining default configuration for building docker images for akka based service implementations.
  */
object ServicePackagingPlugin extends AutoPlugin {

  override lazy val requires = UniversalPlugin && JavaAppPackaging && DockerPlugin

  override lazy val trigger = noTrigger

  override lazy val projectSettings = Seq(
    maintainer         := "Nexus Team <noreply@epfl.ch>",
    dockerBaseImage    := "openjdk:8-jre",
    daemonUser         := "root",
    dockerExposedPorts := Seq(8080, 2552),
    dockerRepository   := sys.env.get("DOCKER_REGISTRY"),
    dockerAlias := DockerAlias(dockerRepository.value,
                               None,
                               (packageName in Docker).value,
                               Some((version in Docker).value)),
    dockerBuildOptions ++= {
      val options = for {
        stringArgs <- sys.env.get("DOCKER_BUILD_ARGS").toList
        arg        <- stringArgs.split(Pattern.quote("|"))
        pair       <- List("--build-arg", arg)
      } yield pair
      options
    },
    dockerUpdateLatest          := !isSnapshot.value,
    defaultLinuxInstallLocation := "/opt/nexus",
    dockerCommands := {
      val current = dockerCommands.value.filterNot {
        case Cmd("FROM", _) => true
        case Cmd("USER", _) => true
        case _              => false
      }
      val top = Seq(
        Cmd("FROM", dockerBaseImage.value),
        Cmd("USER", daemonUser.value),
        ExecCmd("RUN", "apt-get", "-qq", "update"),
        ExecCmd("RUN", "apt-get", "-yq", "install", "dnsutils"),
        ExecCmd("RUN", "apt-get", "clean")
      )
      val last =
        Seq(ExecCmd("RUN", "chown", "-R", "root:0", "/opt/docker"), ExecCmd("RUN", "chmod", "-R", "g+w", "/opt/docker"))
      top ++ current ++ last
    },
    publishLocal := {
      publishLocal.value
      Def.taskDyn {
        if (!isSnapshot.value) Def.task { (publishLocal in Docker).value } else Def.task { () }
      }.value
    },
    publish := {
      publish.value
      Def.taskDyn {
        if (!isSnapshot.value) Def.task { (publish in Docker).value } else Def.task { () }
      }.value
    }
  )
}
