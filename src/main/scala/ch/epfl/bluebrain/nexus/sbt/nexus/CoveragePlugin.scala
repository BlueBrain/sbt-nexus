package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt._
import scoverage.ScoverageSbtPlugin
import scoverage.ScoverageSbtPlugin.autoImport._

/**
  * Default configuration for measuring / enforcing code coverage (configures scoverage plugin's settings).
  */
object CoveragePlugin extends AutoPlugin {

  override lazy val requires = ScoverageSbtPlugin

  override lazy val trigger = allRequirements

  override lazy val projectSettings = Seq(coverageMinimum := 80, coverageFailOnMinimum := true)
}
