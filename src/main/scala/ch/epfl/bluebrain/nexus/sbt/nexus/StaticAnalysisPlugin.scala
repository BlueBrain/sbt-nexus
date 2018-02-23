package ch.epfl.bluebrain.nexus.sbt.nexus

import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import sbt._

/**
  * Default configuration for static analysis (configures scapegoat plugin's settings).
  */
object StaticAnalysisPlugin extends AutoPlugin {

  override lazy val requires = ScapegoatSbtPlugin

  override lazy val trigger = allRequirements

  override lazy val buildSettings = Seq(
    scapegoatVersion := "1.3.4",
    scapegoatMaxWarnings := 0,
    scapegoatMaxErrors := 0,
    scapegoatMaxInfos := 0,
    scapegoatDisabledInspections := Seq(
      "RedundantFinalModifierOnCaseClass",
      "RedundantFinalModifierOnMethod",
      "ObjectNames",
      "AsInstanceOf",
      "ClassNames"
    )
  )
}
