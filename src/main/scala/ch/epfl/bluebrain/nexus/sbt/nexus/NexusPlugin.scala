package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt._
import sbt.Keys._

/**
  * Generic plugin to define arbitrary Nexus specific settings that don't deserve their own separate plugin.
  */
object NexusPlugin extends AutoPlugin {

  override lazy val requires = empty

  override lazy val trigger = allRequirements

  override lazy val projectSettings = Seq(organization := "ch.epfl.bluebrain.nexus")
}
