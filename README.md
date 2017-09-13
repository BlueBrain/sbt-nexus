## SBT Nexus Plugin

The plugin provides a collection of sbt auto plugins that configure a project with reasonable defaults for most sbt
based scala projects.

### Getting Started

Add the following line to your `project/plugins.sbt` file:

```scala
addSbtPlugin("ch.epfl.bluebrain.nexus" % "sbt-nexus" % "M.m.p")
```

Add the following environment variables (you can safely exclude the optional ones):
```
export RELEASES_REPOSITORY="https://my-repo/repository/content/repositories/releases"
export SNAPSHOTS_REPOSITORY="https://my-repo/repository/content/repositories/snapshots"
export ADDITIONAL_RESOLVERS="https://resolver1/repository/content/groups/public|https://resolver2/repository/content/groups/public" #optional
export DOCKER_REGISTRY="my-registry.com/my-project" # optional
export DOCKER_BUILD_ARGS="HTTP_PROXY='http://my-proxy:80'|HTTPS_PROXY='http://my-proxy:80'|no_proxy='localhost,127.0.0.1'" #optional
```

Environment variables that support multiple values are strings separated by '|'.

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

The plugins automatically sets the `scalaVersion` to `2.12.3`, the `javaSpecificationVersion` to `1.8` and checks during
the initialization phase if the installed jdk is compatible with the target java version.  It also appends to the
`scalacOptions` all the flags defined in the exposed settings and sets the necessary compiler flags for both `javac` and
`scalac` to generate bytecode compatible with the defined `javaSpecificationVersion`.

#### ReleasePlugin

The `ReleasePlugin` configures the release process with explicit steps and computes the release and next versions based
on environment variables (if present).

Issue the following command to initiate a release: `sbt 'release with-defaults'`

The configured release process steps are configured as follows:
   1. check the working dir is clean
   2. check the project has no snapshot dependencies
   3. compute the appropriate release and next development version
   4. clean the project
   5. run tests (implies compile)
   6. update the `version.sbt` with the release version
   7. commit the changes to `version.sbt`
   8. tag the release with __$artifactId-$version__
   9. publish the artifact according to the publish settings
   10. update the `version.sbt` with the next development version
   11. commit the changes to `version.sbt`
   12. push the changes to the remote git repository

By default, the current configuration releases the artifact using the version defined in `version.sbt` with its
__-SNAPSHOT__ qualifier stripped.  The next version is computed by incrementing the patch (bugfix) segment of the
release version.  For instance, if the current version is __0.3.5-SNAPSHOT__ the release version will be set to
__0.3.5__ and the next development version will be set to __0.3.6-SNAPSHOT__.

In order to change the default computed versions, for instance in situations where you need to release a new minor or
major version, you can pass to SBT the following environment variables:

   * `RELEASE_VERSION`: the exact version to release (__note:__ if it contains a qualifier it will be stripped)
   * `NEXT_VERSION`: the next development version to be added to the `version.sbt` file

Both of these variables are optional.  For example:

```shell
$ sbt 'release with-defaults'
$ RELEASE_VERSION="0.4.0" sbt 'release with-defaults'
$ RELEASE_VERSION="0.4.0" NEXT_VERSION="0.5.0-SNAPSHOT" sbt 'release with-defaults'
$ NEXT_VERSION="0.5.0-SNAPSHOT" sbt 'release with-defaults'
```

#### PublishPlugin

The `PublishPlugin` configures the target repositories where snapshots or releases are to be published and defines a
default internal resolver for fetching artifacts.

Exposed setting keys:

   * `releasesRepository` (_releases-repository_): the target repository for publishing releases
   * `snapshotsRepository` (_snapshots-repository_): the target repository for publishing snapshots
   * `additionalResolvers` (_additional-resolvers_): a collection of resolvers to add to the build
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

#### ServicePackagingPlugin

The `ServicePackagingPlugin` automates the configuration for creating and publishing docker images.  The auto plugin
needs to be enabled _explicitly_ along with its dependencies, i.e.:

```scala
lazy val myProject = project
  .enablePlugins(UniversalPlugin, JavaAppPackaging, DockerPlugin, ServicePackagingPlugin)
  .settings(packageName in Docker := "my-image-name")
```

Enabling the plugin also requires setting the appropriate image name, as in the example above.

The docker image is being built referencing `openjdk:8-jre` as the parent image.

The plugin also hooks into the `publishLocal` and `publish` tasks to automatically publish the produced docker image
to the configured repository.

Please see the [sbt native packager documentation](http://www.scala-sbt.org/sbt-native-packager/) for more information
about possible configuration options.

#### VersionPlugin

The `VersionPlugin` has the sole purpose of pulling in the configuration keys for versions of well known dependencies
automatically in the scope of the `build.sbt` file.  It allows a consistent upgrade of dependency versions across all
projects built with this plugin.

Projects can override the versions inherited from this plugin, for example:

```scala
lazy val myProject = project
  .settings(circeVersion := "0.6.1") // overrides the default version set to "0.7.0"
  .settings(libraryDependencies ++= Seq(
    "io.circe"      %% "circe-core" % circeVersion.value,
    "org.scalatest" %% "scalatest"  % scalaTestVersion.value % Test
  ))
```

#### NexusPlugin

Generic auto plugin to define arbitrary Nexus specific settings that don't deserve their own separate plugin.  It
currently configures the default organization for all artifacts.