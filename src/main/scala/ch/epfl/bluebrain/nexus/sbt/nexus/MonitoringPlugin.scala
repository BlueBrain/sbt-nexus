package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt.Keys.libraryDependencies
import sbt._

object MonitoringPlugin extends AutoPlugin {

  override lazy val requires = ServicePackagingPlugin

  override lazy val trigger = allRequirements

  override lazy val projectSettings = Seq(
    libraryDependencies ++= Seq(
      "io.kamon" %% "kamon-core"            % "1.0.1",
      "io.kamon" %% "kamon-prometheus"      % "1.0.0",
      "io.kamon" %% "kamon-jaeger"          % "1.0.1",
      "io.kamon" %% "kamon-akka-http-2.5"   % "1.0.1",
      "io.kamon" %% "kamon-akka-2.5"        % "1.0.1" % Runtime,
      "io.kamon" %% "kamon-akka-remote-2.5" % "1.0.0" % Runtime,
      "io.kamon" %% "kamon-system-metrics"  % "1.0.0" % Runtime
    )
  )
}
