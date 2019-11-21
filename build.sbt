/*
scalafmt: {
  style = defaultWithAlign
  maxColumn = 150
  align.tokens = [
    { code = "=>", owner = "Case" }
    { code = "?", owner = "Case" }
    { code = "extends", owner = "Defn.(Class|Trait|Object)" }
    { code = "//", owner = ".*" }
    { code = "{", owner = "Template" }
    { code = "}", owner = "Template" }
    { code = ":=", owner = "Term.ApplyInfix" }
    { code = "++=", owner = "Term.ApplyInfix" }
    { code = "+=", owner = "Term.ApplyInfix" }
    { code = "%", owner = "Term.ApplyInfix" }
    { code = "%%", owner = "Term.ApplyInfix" }
    { code = "%%%", owner = "Term.ApplyInfix" }
    { code = "->", owner = "Term.ApplyInfix" }
    { code = "?", owner = "Term.ApplyInfix" }
    { code = "<-", owner = "Enumerator.Generator" }
    { code = "?", owner = "Enumerator.Generator" }
    { code = "=", owner = "(Enumerator.Val|Defn.(Va(l|r)|Def|Type))" }
  ]
}
 */

// Main plugin settings
organization        := "ch.epfl.bluebrain.nexus"
name                := "sbt-nexus"
sbtPlugin           := true
dependencyBlacklist := moduleFilter(NothingFilter, NothingFilter, NothingFilter)

// Build publish settings
inThisBuild(
  List(
    homepage := Some(url("https://github.com/BlueBrain/sbt-nexus")),
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scmInfo  := Some(ScmInfo(url("https://github.com/BlueBrain/sbt-nexus"), "scm:git:git@github.com:BlueBrain/sbt-nexus.git")),
    developers := List(
      Developer("bogdanromanx", "Bogdan Roman", "noreply@epfl.ch", url("https://bluebrain.github.io/nexus")),
      Developer("hygt", "Henry Genet", "noreply@epfl.ch", url("https://bluebrain.github.io/nexus")),
      Developer("umbreak", "Didac Montero Mendez", "noreply@epfl.ch", url("https://bluebrain.github.io/nexus")),
      Developer("wwajerowicz", "Wojtek Wajerowicz", "noreply@epfl.ch", url("https://bluebrain.github.io/nexus")),
    ),
    // These are the sbt-release-early settings to configure
    releaseEarlyWith              := BintrayPublisher,
    releaseEarlyNoGpg             := true,
    releaseEarlyEnableSyncToMaven := false,
  ))

// Additional plugins to introduce to projects using this plugin
addSbtPlugin("ch.epfl.scala"          % "sbt-release-early"   % "2.1.1")
addSbtPlugin("org.scalameta"          % "sbt-scalafmt"        % "2.2.1")
addSbtPlugin("com.typesafe.sbt"       % "sbt-native-packager" % "1.4.0")
addSbtPlugin("org.scoverage"          % "sbt-scoverage"       % "1.6.1")
addSbtPlugin("com.sksamuel.scapegoat" % "sbt-scapegoat"       % "1.1.0")
addSbtPlugin("com.lightbend.paradox"  % "sbt-paradox"         % "0.6.7")

addCommandAlias("review", ";clean;scalafmtCheck;scalafmtSbtCheck;compile")
