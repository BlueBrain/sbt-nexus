package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt.Keys._
import sbt._

/**
  * Provides default settings and utility for properly mapping dependent artifacts to their online documentation for
  * cross linking the scala doc.  The default configuration automatically adds the ''scala library'' to the
  * ''apiMappings''.
  *
  * @see [[ch.epfl.bluebrain.nexus.sbt.nexus.DocumentationPlugin#mapping]]
  */
object DocumentationPlugin extends AutoPlugin {

  override lazy val requires = CompilationPlugin

  override lazy val trigger = allRequirements

  trait Keys {
    val suppressLinkWarnings =
      SettingKey[Boolean]("doc-suppress-link-warnings", "Suppress the documentation linking warnings.")
  }
  object autoImport extends Keys
  import autoImport._

  override lazy val projectSettings = Seq(
    suppressLinkWarnings := true,
    scalacOptions in (Compile, doc) ++= {
      if (suppressLinkWarnings.value) Seq("-no-link-warnings")
      else Seq.empty[String]
    },
    javacOptions in (Compile, doc) := Seq("-source", CompilationPlugin.autoImport.javaSpecificationVersion.value),
    autoAPIMappings                := true,
    apiMappings += {
      val scalaDocUrl = s"http://scala-lang.org/api/${scalaVersion.value}/"
      apiMappingFor((fullClasspath in Compile).value)("scala-library", scalaDocUrl)
    }
  )

  /**
    * <p>Constructs a tuple (java.io.File -> sbt.URL) by searching the ''classpath'' for the jar file with the argument
    * ''artifactId''.  The value returned can be appended to the ''apiMappings'' such that documentation linking works
    * when building the docs.</p>
    *
    * <p>For example:
    * {{{
    *   import ch.epfl.bluebrain.nexus.sbt.nexus.DocumentationPlugin.apiMappingFor
    *   import sbt.Keys._
    *
    *   lazy val myProject = project.settings(Seq(
    *     apiMappings += {
    *       val scalaDocUrl = "http://scala-lang.org/api/" + scalaVersion.value + "/"
    *       apiMappingFor((fullClasspath in Compile).value)("scala-library", scalaDocUrl)
    *     }
    *   ))
    * }}}
    * </p>
    *
    * @param classpath  the classpath to search
    * @param artifactId the artifact that provides the linked type
    * @param address    the full URL (as string) of the artifact documentation
    * @return a tuple (java.io.File -> sbt.URL) to be appended to the ''apiMappings''
    */
  final def apiMappingFor(classpath: Seq[Attributed[File]])(artifactId: String, address: String): (File, URL) = {
    def findJar(nameBeginsWith: String): File = {
      classpath
        .find { attributed: Attributed[java.io.File] =>
          (attributed.data ** s"$nameBeginsWith*.jar").get.nonEmpty
        }
        .get
        .data // fail hard if not found
    }
    findJar(artifactId) -> url(address)
  }
}
