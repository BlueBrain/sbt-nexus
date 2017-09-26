// Main plugin settings
organization           := "ch.epfl.bluebrain.nexus"
name                   := "sbt-nexus"
sbtPlugin              := true
overrideBuildResolvers := true
publishMavenStyle      := true
pomIncludeRepository   := Function.const(false)
publishTo              := {
  if (isSnapshot.value) Some("Snapshots" at sys.env("SNAPSHOTS_REPOSITORY"))
  else Some("Releases" at sys.env("RELEASES_REPOSITORY"))
}

// Release settings
import sbtrelease.ReleaseStateTransformations._
import sbtrelease._
releaseVersionBump  := Version.Bump.Bugfix
releaseVersion      := { ver =>
  sys.env.get("RELEASE_VERSION")                                // fetch the optional system env var
    .map(_.trim)
    .filterNot(_.isEmpty)
    .map(v => Version(v).getOrElse(versionFormatError))         // parse it into a version or throw
    .orElse(Version(ver).map(_.withoutQualifier))               // fallback on the current version without a qualifier
    .map(_.string)                                              // map it to its string representation
    .getOrElse(versionFormatError)                              // throw if we couldn't compute the version
}
releaseNextVersion   := { ver =>
  sys.env.get("NEXT_VERSION")                                   // fetch the optional system env var
    .map(_.trim)
    .filterNot(_.isEmpty)
    .map(v => Version(v).getOrElse(versionFormatError))         // parse it into a version or throw
    .orElse(Version(ver).map(_.bump(releaseVersionBump.value))) // fallback on the current version bumped accordingly
    .map(_.asSnapshot.string)                                   // map it to its snapshot version as string
    .getOrElse(versionFormatError)                              // throw if we couldn't compute the version
}
releaseCrossBuild    := false
releaseTagName       := s"v${(version in ThisBuild).value}"
releaseTagComment    := s"Releasing version ${(version in ThisBuild).value}"
releaseCommitMessage := s"Setting new version to ${(version in ThisBuild).value}"
releaseProcess       := Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges)

// Command aliases for CI
addCommandAlias("review", ";clean;package")
addCommandAlias("rel",    ";release with-defaults")

// Additional plugins to introduce to projects using this plugin
addSbtPlugin("org.scoverage"          %% "sbt-scoverage"       % "1.5.1")
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat"       % "1.0.7")
addSbtPlugin("com.github.gseitz"       % "sbt-release"         % "1.0.6")
addSbtPlugin("com.typesafe.sbt"        % "sbt-native-packager" % "1.2.2")
