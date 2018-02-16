package ch.epfl.bluebrain.nexus.sbt.nexus

import java.util.Properties

import bintray.BintrayPlugin.autoImport.{bintrayOrganization, bintrayRepository}
import ch.epfl.scala.sbt.release.ReleaseEarlyPlugin
import sbt.Keys._
import sbt._
import sbt.librarymanagement.ModuleFilter

import scala.sys.process._
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, Node, NodeSeq}

/**
  * Release configuration for projects using the plugin.  The versions used in the release can be overridden by means
  * of environment variables, specifically ''RELEASE_VERSION'' and ''NEXT_VERSION''.
  *
  * If the ''RELEASE_VERSION'' is omitted its value will be computed by striping the ''-SNAPSHOT'' suffix of the
  * current version (as listed in ''version.sbt'').
  *
  * If the ''NEXT_VERSION'' property is its value will be computed based on the resulting value of the
  * ''RELEASE_VERSION'' and the default bump strategy configured by ''releaseVersionBump'' (release + bump).
  */
object ReleasePlugin extends AutoPlugin {

  trait Keys {
    val dependencyBlacklist = SettingKey[ModuleFilter](
      "dependency-blacklist",
      "A module filter which indicates if a module should be removed from the resulting pom file; if the filter" +
        " returns true, the module is removed from the dependency list"
    )
    val buildInfoProperties = taskKey[File]("Generates build info properties")
  }
  object autoImport extends Keys
  import autoImport._

  override lazy val requires = ReleaseEarlyPlugin

  override lazy val trigger = allRequirements

  override lazy val projectSettings = Seq(
    bintrayOrganization := Some("bbp"),
    bintrayRepository := {
      import ch.epfl.scala.sbt.release.ReleaseEarly.Defaults
      if (Defaults.isSnapshot.value) "nexus-snapshots"
      else "nexus-releases"
    },
    sources in (Compile, doc)                := Seq.empty,
    publishArtifact in packageDoc            := false,
    publishArtifact in (Compile, packageSrc) := true,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Test, packageBin)    := false,
    publishArtifact in (Test, packageDoc)    := false,
    publishArtifact in (Test, packageSrc)    := false,
    publishMavenStyle                        := true,
    pomIncludeRepository                     := Function.const(false),
    // predefined modules to be excluded from the dependency list of the resulting pom
    dependencyBlacklist := {
      moduleFilter("org.scoverage") | moduleFilter("com.sksamuel.scapegoat")
    },
    // removes compile time only dependencies from the resulting pom
    pomPostProcess := { node =>
      transformer(dependencyBlacklist.value).transform(node).head
    },
    buildInfoProperties := {
      val file            = target.value / "buildinfo.properties"
      val v               = version.value
      val major           = v.split("\\.")(0)
      val minor           = v.split("\\.")(1)
      val contributors    = ("git shortlog -sne HEAD" #| "cut -f2" !!) split "\n"
      val buildProperties = new Properties()
      buildProperties.setProperty("PROJECT", s"nexus-${name.value}")
      buildProperties.setProperty("VERSION", v)
      buildProperties.setProperty("VERSION_MAJOR", major)
      buildProperties.setProperty("VERSION_MINOR", minor)
      buildProperties.setProperty("DESCRIPTION", description.value)
      buildProperties.setProperty("MAINTAINERS", "BlueBrain Nexus Team")
      buildProperties.setProperty("CONTRIBUTORS", contributors.mkString(","))
      buildProperties.setProperty("LICENSE", licenses.value.map(_._1).mkString(","))
      IO.write(buildProperties, "buildinfo", file)
      file
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
