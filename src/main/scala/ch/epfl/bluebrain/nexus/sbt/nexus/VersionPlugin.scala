package ch.epfl.bluebrain.nexus.sbt.nexus

import sbt.{AutoPlugin, SettingKey}

/**
  * Plugin with the sole purpose of pulling in the configuration keys for versions of well known dependencies
  * automatically in the scope of the ''build.sbt'' file.
  */
object VersionPlugin extends AutoPlugin {

  override lazy val requires = empty

  override lazy val trigger = allRequirements

  trait Keys {
    val akkaVersion                     = SettingKey[String]("akka-version")
    val akkaHttpVersion                 = SettingKey[String]("akka-http-version")
    val akkaHttpCorsVersion             = SettingKey[String]("akka-http-cors-version")
    val akkaPersistenceCassandraVersion = SettingKey[String]("akka-persistence-cassandra-version")
    val akkaPersistenceInMemVersion     = SettingKey[String]("akka-persistence-inmemory-version")
    val akkaPersistenceJDBCVersion      = SettingKey[String]("akka-persistence-jdbc-version")
    val akkaHttpCirceVersion            = SettingKey[String]("akka-http-circe-version")
    val aspectjWeaverVersion            = SettingKey[String]("aspectj-weaver-version")
    val catsVersion                     = SettingKey[String]("cats-version")
    val circeVersion                    = SettingKey[String]("circe-version")
    val constructrVersion               = SettingKey[String]("constructr-version")
    val flywayVersion                   = SettingKey[String]("flyway-version")
    val gatlingVersion                  = SettingKey[String]("gatling-version")
    val journalVersion                  = SettingKey[String]("journal-version")
    val kamonVersion                    = SettingKey[String]("kamon-version")
    val kindProjectorVersion            = SettingKey[String]("kind-projector-version")
    val levelDbJniVersion               = SettingKey[String]("leveldb-jni-version")
    val levelDbVersion                  = SettingKey[String]("leveldb-version")
    val logbackVersion                  = SettingKey[String]("logback-version")
    val monixVersion                    = SettingKey[String]("monix-version")
    val nimbusJoseVersion               = SettingKey[String]("nimbus-jose-version")
    val postgreSQLVersion               = SettingKey[String]("postgresql-version")
    val pureconfigVersion               = SettingKey[String]("pureconfig-version")
    val scalaTestVersion                = SettingKey[String]("scalatest-version")
    val shapelessVersion                = SettingKey[String]("shapeless-version")
    val sigarLoaderVersion              = SettingKey[String]("sigar-loader-version")
    val swaggerUIVersion                = SettingKey[String]("swagger-ui-version")
    val typesafeConfigVersion           = SettingKey[String]("typesafe-config-version")
  }
  object autoImport extends Keys
  import autoImport._

  // bind plugin to the project settings
  override lazy val projectSettings = Seq(
    akkaVersion                     := "2.5.4",
    akkaHttpVersion                 := "10.0.10",
    akkaHttpCorsVersion             := "0.2.1",
    akkaPersistenceCassandraVersion := "0.55",
    akkaPersistenceInMemVersion     := "2.5.1.1",
    akkaPersistenceJDBCVersion      := "2.5.2.0",
    akkaHttpCirceVersion            := "1.18.0",
    aspectjWeaverVersion            := "1.8.10",
    catsVersion                     := "0.9.0",
    circeVersion                    := "0.8.0",
    constructrVersion               := "0.17.0",
    flywayVersion                   := "4.2.0",
    gatlingVersion                  := "2.2.4",
    journalVersion                  := "3.0.18",
    kamonVersion                    := "0.6.8",
    kindProjectorVersion            := "0.9.3",
    levelDbJniVersion               := "1.8",
    levelDbVersion                  := "0.7",
    logbackVersion                  := "1.2.3",
    monixVersion                    := "2.3.0",
    nimbusJoseVersion               := "4.39.1",
    postgreSQLVersion               := "42.1.1",
    pureconfigVersion               := "0.8.0",
    scalaTestVersion                := "3.0.4",
    shapelessVersion                := "2.3.2",
    sigarLoaderVersion              := "1.6.6-rev002",
    swaggerUIVersion                := "3.0.14",
    typesafeConfigVersion           := "1.3.1"
  )
}
