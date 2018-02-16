package ch.epfl.bluebrain.nexus.sbt.nexus

import com.typesafe.sbt.packager.Keys.bashScriptExtraDefines
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin
import sbt.Keys.libraryDependencies
import sbt._

object InstrumentationPlugin extends AutoPlugin {

  override lazy val requires = UniversalPlugin && JavaAppPackaging && BashStartScriptPlugin

  override lazy val trigger = allRequirements

  trait Keys {
    val aspectjWeaverVersion = SettingKey[String]("aspectj-weaver-version", "AspectJ weaver version")
    val sigarLoaderVersion   = SettingKey[String]("sigar-loader-version", "Kamon Sigar loader version")
  }

  object autoImport extends Keys
  import autoImport._

  override lazy val projectSettings = Seq(
    aspectjWeaverVersion := "1.8.10",
    sigarLoaderVersion   := "1.6.6-rev002",
    libraryDependencies ++= Seq("org.aspectj" % "aspectjweaver" % aspectjWeaverVersion.value % Runtime,
      "io.kamon" % "sigar-loader" % sigarLoaderVersion.value % Runtime),
    bashScriptExtraDefines ++= Seq(
      s"""addJava "-javaagent:$$lib_dir/org.aspectj.aspectjweaver-${aspectjWeaverVersion.value}.jar"""",
      s"""addJava "-javaagent:$$lib_dir/io.kamon.sigar-loader-${sigarLoaderVersion.value}.jar""""
    )
  )
}
