package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt.Keys._
import sbt._

/**
  * Keys and default configuration for project compilation.  Aside the typical compiler flags for both scala and java
  * there's also a java version compatibility check that kicks in during the project initialization phase.  If the
  * java version installed on the machine is incompatible with the expected version defined in the configuration the
  * build will automatically error out.
  */
object CompilationPlugin extends AutoPlugin {

  override lazy val requires = empty

  override lazy val trigger = allRequirements

  trait Keys {
    val scalacSilencerVersion = SettingKey[String](
      "scalac-silencer-version",
      "Scalac silencer plugin version for annotation-based warning suppression.")

    val javaSpecificationVersion = SettingKey[String](
      "java-specification-version",
      "The java specification version to be used for source and target compatibility.")

    val scalacCommonFlags =
      SettingKey[Seq[String]]("scalac-common-flags", "Common scalac options useful to most projects")

    val scalacLanguageFlags = SettingKey[Seq[String]]("scalac-language-flags", "Scalac language options to enable")

    val scalacStrictFlags = SettingKey[Seq[String]]("scalac-strict-flags", "Scalac strict compilation flags")

    val scalacOptionalFlags = SettingKey[Seq[String]]("scalac-optional-flags", "Scalac optional compilation flags")
  }

  object autoImport extends Keys
  import autoImport._

  override lazy val projectSettings = Seq(
    javaSpecificationVersion := "1.8",
    scalaVersion             := "2.12.6",
    scalacSilencerVersion    := "1.2",
    scalacCommonFlags        := Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-unchecked", "-Xlint"),
    scalacLanguageFlags := Seq(
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-language:existentials",
      "-language:experimental.macros"
    ),
    scalacStrictFlags := Seq(
      "-Xfatal-warnings",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-inaccessible",
      "-Ywarn-unused-import",
      "-Ywarn-unused:params,patvars",
      "-Ywarn-macros:after",
      "-Xfuture"
    ),
    scalacOptionalFlags := Seq("-Ypartial-unification"),
    scalacOptions ++= {
      scalacCommonFlags.value ++
        scalacLanguageFlags.value ++
        scalacStrictFlags.value ++
        scalacOptionalFlags.value ++
        Seq(s"-target:jvm-${javaSpecificationVersion.value}")
    },
    scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Xfatal-warnings")),
    javacOptions ++= Seq("-source",
                         javaSpecificationVersion.value,
                         "-target",
                         javaSpecificationVersion.value,
                         "-Xlint"),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" %% "silencer-plugin" % scalacSilencerVersion.value),
      "com.github.ghik" %% "silencer-lib" % scalacSilencerVersion.value
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
