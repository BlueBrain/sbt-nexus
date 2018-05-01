[![Join the chat at https://gitter.im/BlueBrain/nexus](https://badges.gitter.im/BlueBrain/nexus.svg)](https://gitter.im/BlueBrain/nexus?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](http://jenkins.nexus.ocp.bbp.epfl.ch/buildStatus/icon?job=nexus/sbt-nexus/master)](http://jenkins.nexus.ocp.bbp.epfl.ch/job/nexus/sbt-nexus/master)
[![GitHub release](https://img.shields.io/github/release/BlueBrain/sbt-nexus.svg)]()

## SBT Nexus Plugin

The plugin provides a collection of sbt auto plugins that configure a project with reasonable defaults for most sbt
based scala nexus projects.

Please visit the [parent project](https://github.com/BlueBrain/nexus) for more information about Nexus.

### Getting Started

Add the necessary resolver:

```scala
resolvers += Resolver.bintrayRepo("bbp", "nexus-releases")
```

Add the following line to your `project/plugins.sbt` file:

```scala
addSbtPlugin("ch.epfl.bluebrain.nexus" % "sbt-nexus" % "M.m.p")
```

### Plugins

#### CompilationPlugin

The `CompilationPlugin` defines all the settings related to compilation.  It has no dependencies and is set to trigger
automatically.

Exposed setting keys:

   * `javaSpecificationVersion` (_java-specification-version_): java version to be used for source and target compatibility
   * `scalacCommonFlags` (_scalac-common-flags_): common scalac options useful to most projects
   * `scalacLanguageFlags` (_scalac-language-flags_): language options to enable
   * `scalacStrictFlags` (_scalac-strict-flags_): a collection of stricter compilation flags (i.e.: `-Xfatal-warnings`)
   * `scalacOptionalFlags` (_scalac-optional-flags_): useful additional flags (i.e.: `-Ypartial-unification`)

The plugins automatically sets the `scalaVersion` to `2.12.6`, the `javaSpecificationVersion` to `1.8` and checks during
the initialization phase if the installed jdk is compatible with the target java version.  It also appends to the
`scalacOptions` all the flags defined in the exposed settings and sets the necessary compiler flags for both `javac` and
`scalac` to generate bytecode compatible with the defined `javaSpecificationVersion`.

#### ReleasePlugin

The `ReleasePlugin` configures the release process using for `sbt-release-early` plugin.

Additional exposed setting keys:

   * `dependencyBlacklist` (_dependency-blacklist_): a module filter for stripping out compile time only dependencies
     from the resulting pom file.

The plugin hooks into the `pomPostProcess` task and removes the _dependency_ xml nodes that match the filter defined by
the `dependencyBlacklist`.  The filter is currently configured to strip out coverage and static analysis dependencies
(_scoverage_ and _scapegoat_).


#### StaticAnalysisPlugin

The `StaticAnalysisPlugin` pulls into the project default settings for the `ScapegoatSbtPlugin`.  Run the following
task for static analysis: `scapegoat`.

#### CoveragePlugin

The `CoveragePlugin` pulls into the project default settings for the `ScoverageSbtPlugin`:

   * require at least 80% code coverage
   * fail the build if the coverage target is not met

To enable code coverage measurement issue the `coverage` command and run the tests.  To compute the resulting coverage
run the `coverageReport` task.

To disable code coverage measurement issue the `coverageOff` command.

To produce an aggregate coverage report for multi-module builds run the `coverageAggregate` task.

__Note:__ make sure to disable code coverage measurement and recompile the project before attempting to run or publish
the project to ensure that the produced bytecode is not instrumented for coverage.

#### DocumentationPlugin

The `DocumentationPlugin` defines settings and an utility to facilitate cross linking scala documentation when comments
contain references to types that have documentation (scala doc) published online.

If scala doc comments contain references to types in dependent libraries and the project is not configured with the
correct mapping (artifactId -> url) attempting to build the doc will fail if the link warnings are not suppressed (the
setting to suppress these warnings is defined under the `suppressLinkWarnings` key and defaults to `true`).

Adding an external documentation reference based on an artifactId and enabling link warnings:

```scala
import ch.epfl.bluebrain.nexus.sbt.nexus.DocumentationPlugin.apiMappingFor
import sbt.Keys._

lazy val myProject = project.settings(Seq(
  suppressLinkWarnings := false,
  apiMappings          += {
    val scalaDocUrl = "http://scala-lang.org/api/" + scalaVersion.value + "/"
    apiMappingFor((fullClasspath in Compile).value)("scala-library", scalaDocUrl)
  }
))
```

The default configuration automatically adds the _scala library_ documentation url to the _apiMappings_.

#### InstrumentationPlugin

This plugin adds aspectj and sigar loader as java agents to all apps packaged with universal packaging.

#### NexusPlugin

Generic auto plugin to define arbitrary Nexus specific settings that don't deserve their own separate plugin.  It
currently configures the default organization for all artifacts.
