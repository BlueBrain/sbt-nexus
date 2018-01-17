package ch.epfl.bluebrain.nexus.sbt.nexus

import _root_.bintray.BintrayPlugin
import _root_.bintray.BintrayKeys._
import sbt.Keys._
import sbt._
import sbt.librarymanagement.ModuleFilter

import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, Node, NodeSeq}

/**
  * Keys and default configuration for publishing artifacts either locally or remote.
  */
object PublishPlugin extends AutoPlugin {

  override lazy val requires = BintrayPlugin

  override lazy val trigger = allRequirements

  trait Keys {
    val dependencyBlacklist = SettingKey[ModuleFilter](
      "dependency-blacklist",
      "A module filter which indicates if a module should be removed from the resulting pom file; if the filter" +
        " returns true, the module is removed from the dependency list"
    )
  }
  object autoImport extends Keys
  import autoImport._

  override lazy val projectSettings = Seq(
    bintrayOrganization     := Some("bbp"),
    bintrayRepository       := "nexus-releases",
    publishMavenStyle       := true,
    publishArtifact in Test := false,
    pomIncludeRepository    := Function.const(false),
    // predefined modules to be excluded from the dependency list of the resulting pom
    dependencyBlacklist := {
      moduleFilter("org.scoverage") | moduleFilter("com.sksamuel.scapegoat")
    },
    // removes compile time only dependencies from the resulting pom
    pomPostProcess := { node =>
      transformer(dependencyBlacklist.value).transform(node).head
    }
  )

  /**
    * Constructs a new XML [[scala.xml.transform.RuleTransformer]] based on the argument ''blacklist'' module filter
    * that strips out ''dependency'' xml nodes that are matched by the ''blacklist''.
    *
    * @param blacklist the filter to apply to dependencies
    * @return a new XML [[scala.xml.transform.RuleTransformer]] that strips out ''blacklist''ed dependencies
    */
  private def transformer(blacklist: ModuleFilter): RuleTransformer =
    new RuleTransformer(new RewriteRule {
      override def transform(node: Node): NodeSeq = node match {
        case e: Elem if e.label == "dependency" =>
          val organization = e.child.filter(_.label == "groupId").flatMap(_.text).mkString
          val artifact     = e.child.filter(_.label == "artifactId").flatMap(_.text).mkString
          val version      = e.child.filter(_.label == "version").flatMap(_.text).mkString
          if (blacklist(organization % artifact % version)) NodeSeq.Empty else node
        case _ => node
      }
    })
}
