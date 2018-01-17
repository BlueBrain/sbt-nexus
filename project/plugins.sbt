resolvers += Resolver.bintrayRepo("bogdanromanx", "maven")

addSbtPlugin("com.github.gseitz"                         % "sbt-release"  % "1.0.7")
addSbtPlugin("com.geirsson"                              % "sbt-scalafmt" % "1.4.0")
addSbtPlugin("com.github.bogdanromanx.org.foundweekends" % "sbt-bintray"  % "0.5.2-nexus1")
