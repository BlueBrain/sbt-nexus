package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt._
import sbt.Keys.libraryDependencies
import sbt.{AutoPlugin, SettingKey}

object MonitoringPlugin extends AutoPlugin {

  override lazy val requires = empty

  override lazy val trigger = allRequirements

  trait Keys {
    val kamonVersion = SettingKey[String]("kamon-version", "Kamon version")
  }

  object autoImport extends Keys
  import autoImport._

  override lazy val projectSettings = Seq(
    kamonVersion := "1.0.0",
    libraryDependencies ++= Seq(
      "io.kamon" %% "kamon-core"          % kamonVersion.value,
      "io.kamon" %% "kamon-akka-2.5"      % kamonVersion.value,
      "io.kamon" %% "kamon-akka-http-2.5" % kamonVersion.value,
      "io.kamon" %% "kamon-prometheus"    % kamonVersion.value,
      "io.kamon" %% "kamon-jaeger"        % kamonVersion.value
    )
  )
}
