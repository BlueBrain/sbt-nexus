package ch.epfl.bluebrain.nexus.sbt.nexus

import java.util.Properties

import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease._
import scala.sys.process._

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

  lazy val buildInfoProperties  = taskKey[File]("Generates build info properties")

  override lazy val requires = sbtrelease.ReleasePlugin

  override lazy val trigger = allRequirements

  override lazy val projectSettings = Seq(
    buildInfoProperties := {
      val file = target.value / "buildinfo.properties"
      val v = version.value
      val major = v.split("\\.")(0)
      val minor = v.split("\\.")(1)
      val contributors = ("git shortlog -sne HEAD" #| "cut -f2" !!) split "\n"
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
    },
    // bump the patch (bugfix) version by default
    releaseVersionBump   := Version.Bump.Bugfix,
    // compute the version to use for the release (from sys.env or version.sbt)
    releaseVersion       := { ver =>
      sys.env.get("RELEASE_VERSION")                                // fetch the optional system env var
        .map(_.trim)
        .filterNot(_.isEmpty)
        .map(v => Version(v).getOrElse(versionFormatError))         // parse it into a version or throw
        .orElse(Version(ver).map(_.withoutQualifier))               // fallback on the current version without a qualifier
        .map(_.string)                                              // map it to its string representation
        .getOrElse(versionFormatError)                              // throw if we couldn't compute the version
    },
    // compute the next development version to use for the release (from sys.env or release version)
    releaseNextVersion   := { ver =>
      sys.env.get("NEXT_VERSION")                                   // fetch the optional system env var
        .map(_.trim)
        .filterNot(_.isEmpty)
        .map(v => Version(v).getOrElse(versionFormatError))         // parse it into a version or throw
        .orElse(Version(ver).map(_.bump(releaseVersionBump.value))) // fallback on the current version bumped accordingly
        .map(_.asSnapshot.string)                                   // map it to its snapshot version as string
        .getOrElse(versionFormatError)                              // throw if we couldn't compute the version
    },
    // never cross build
    releaseCrossBuild    := false,
    // tag the release with the '$artifactId-$version'
    releaseTagName       := s"v${(version in ThisBuild).value}",
    // tag commit comment
    releaseTagComment    := s"Releasing version ${(version in ThisBuild).value}",
    // the message to use when committing the new version to version.sbt
    releaseCommitMessage := s"Setting new version to ${(version in ThisBuild).value}",
    // the default release process, listed explicitly
    releaseProcess       := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      releaseStepTask(buildInfoProperties),
      setNextVersion,
      commitNextVersion,
      pushChanges))
}
