package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt.Keys._
import sbt._
import _root_.io.github.davidgregory084.TpolecatPlugin

/**
  * Keys and default configuration for project compilation.  Aside the typical compiler flags for both scala and java
  * there's also a java version compatibility check that kicks in during the project initialization phase.  If the
  * java version installed on the machine is incompatible with the expected version defined in the configuration the
  * build will automatically error out.
  */
object CompilationPlugin extends AutoPlugin {

  override lazy val requires = TpolecatPlugin

  override lazy val trigger = allRequirements

  trait Keys {
    val scalacSilencerVersion = SettingKey[String](
      "scalac-silencer-version",
      "Scalac silencer plugin version for annotation-based warning suppression."
    )

    val javaSpecificationVersion = SettingKey[String](
      "java-specification-version",
      "The java specification version to be used for source and target compatibility."
    )
  }

  object autoImport extends Keys
  import autoImport._

  override lazy val projectSettings = Seq(
    javaSpecificationVersion := "11",
    scalaVersion             := "2.13.1",
    scalacSilencerVersion    := "1.4.4",
    scalacOptions ~= filterSelfImplicitScalacOptions,
    javacOptions ++= Seq(
      "-source",
      javaSpecificationVersion.value,
      "-target",
      javaSpecificationVersion.value,
      "-Xlint"
    ),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % scalacSilencerVersion.value cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % scalacSilencerVersion.value % Provided cross CrossVersion.full
    ),
    // fail the build initialization if the JDK currently used is not ${javaSpecificationVersion} or higher
    initialize := {
      // runs the previous initialization
      initialize.value
      // runs the java compatibility check
      val current  = VersionNumber(sys.props("java.specification.version"))
      val required = VersionNumber(javaSpecificationVersion.value)
      assert(CompatibleJavaVersion(current, required), s"Java '$required' or above required; current '$current'")
    }
  )

  val filterSelfImplicitScalacOptions = { options: Seq[String] =>
    options.filterNot(Set("-Wself-implicit"))
  }

  /**
    * Custom java compatibility check.  Any higher version than current is considered compatible.
    */
  object CompatibleJavaVersion extends VersionNumberCompatibility {
    override val name = "Java specification compatibility"

    override def isCompatible(current: VersionNumber, required: VersionNumber): Boolean =
      current.numbers
        .zip(required.numbers)
        .foldRight(required.numbers.size <= current.numbers.size) {
          case ((curr, req), acc) => (curr > req) || (curr == req && acc)
        }

    def apply(current: VersionNumber, required: VersionNumber): Boolean =
      isCompatible(current, required)
  }
}
