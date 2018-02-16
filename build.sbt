// Main plugin settings
organization := "ch.epfl.bluebrain.nexus"
name         := "sbt-nexus"
sbtPlugin    := true

// Build publish settings
inThisBuild(
  List(
    homepage := Some(url("https://github.com/BlueBrain/sbt-nexus")),
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scmInfo := Some(
      ScmInfo(url("https://github.com/BlueBrain/sbt-nexus"), "scm:git:git@github.com:BlueBrain/sbt-nexus.git")),
    developers := List(
      Developer("bogdanromanx", "Bogdan Roman", "noreply@epfl.ch", url("https://bluebrain.epfl.ch/")),
      Developer("hygt", "Henry Genet", "noreply@epfl.ch", url("https://bluebrain.epfl.ch/")),
      Developer("umbreak", "Didac Montero Mendez", "noreply@epfl.ch", url("https://bluebrain.epfl.ch/")),
      Developer("wwajerowicz", "Wojtek Wajerowicz", "noreply@epfl.ch", url("https://bluebrain.epfl.ch/")),
    ),
    // These are the sbt-release-early settings to configure
    releaseEarlyWith              := BintrayPublisher,
    releaseEarlyNoGpg             := true,
    releaseEarlyEnableSyncToMaven := false,
  ))

// Additional plugins to introduce to projects using this plugin
addSbtPlugin("ch.epfl.scala"          % "sbt-release-early"   % "2.1.1")
addSbtPlugin("io.get-coursier"        % "sbt-coursier"        % "1.0.1")
addSbtPlugin("com.geirsson"           % "sbt-scalafmt"        % "1.4.0")
addSbtPlugin("com.typesafe.sbt"       % "sbt-native-packager" % "1.3.3")
addSbtPlugin("org.scoverage"          %% "sbt-scoverage"      % "1.5.1")
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat"      % "1.0.7")
addSbtPlugin("com.lightbend.paradox"  % "sbt-paradox"         % "0.3.2")
addSbtPlugin("com.codacy"             % "sbt-codacy-coverage" % "1.3.11")
