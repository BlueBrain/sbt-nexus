package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease._

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

  override lazy val requires = sbtrelease.ReleasePlugin

  override lazy val trigger = allRequirements

  override lazy val projectSettings = Seq(
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
    releaseTagName       := s"${name.value}-${(version in ThisBuild).value}",
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
      setNextVersion,
      commitNextVersion,
      pushChanges))
}
