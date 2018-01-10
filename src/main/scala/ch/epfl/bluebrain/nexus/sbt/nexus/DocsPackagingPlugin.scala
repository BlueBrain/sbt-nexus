package ch.epfl.bluebrain.nexus.sbt.nexus

import java.util.regex.Pattern

import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.MappingsHelper
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{DockerAlias, Docker}
import com.typesafe.sbt.packager.docker.{Cmd, DockerPlugin}
import sbt.Keys._
import sbt._

/**
  * Plugin defining default configuration for building docker images for paradox based documentation.
  */
object DocsPackagingPlugin extends AutoPlugin {

  override lazy val requires = ParadoxPlugin && DockerPlugin

  override lazy val trigger = noTrigger

  override lazy val projectSettings = Seq(
    maintainer       := "Nexus Team <noreply@epfl.ch>",
    dockerBaseImage  := "nginx:1.11",
    daemonUser       := "root",
    dockerRepository := sys.env.get("DOCKER_REGISTRY"),
    dockerAlias := DockerAlias(dockerRepository.value,
                               None,
                               (packageName in Docker).value,
                               Some((version in Docker).value)),
    dockerBuildOptions ++= {
      val options = for {
        stringArgs <- sys.env.get("DOCKER_BUILD_ARGS").toList
        arg        <- stringArgs.split(Pattern.quote("|"))
        pair       <- List("--build-arg", arg)
      } yield pair
      options
    },
    dockerUpdateLatest := !isSnapshot.value,
    dockerCommands := Seq(
      Cmd("FROM", dockerBaseImage.value),
      Cmd("MAINTAINER", maintainer.value),
      Cmd("USER", daemonUser.value),
      Cmd(
        "RUN",
        "rm -rf /etc/nginx/conf.d && mkdir -p /etc/nginx/conf.d &&  chown -R root:0 /etc/nginx/conf.d && chmod g+rwx /etc/nginx/conf.d"),
      Cmd("RUN", "mkdir -p /var/cache/nginx && chown -R root:0 /var/cache/nginx && chmod g+rwx /var/cache/nginx"),
      Cmd("RUN", "touch /var/run/nginx.pid && chown root:0 /var/run/nginx.pid && chmod g+rwx /var/run/nginx.pid"),
      Cmd("RUN", "ln -sf /dev/stdout /var/log/nginx/access.log && ln -sf /dev/stderr /var/log/nginx/error.log"),
      Cmd("ADD", "default.conf.template /etc/nginx/conf.d"),
      Cmd("ADD", s"${(paradox in Compile).value.getName} /usr/share/nginx/html"),
      Cmd("RUN", "chown -R root:0 /usr/share/nginx/html && chmod g+rwx /usr/share/nginx/html"),
      Cmd("ENV", s"LOCATION /docs"),
      Cmd("ENV", s"SERVER_NAME localhost"),
      Cmd("EXPOSE", "8080"),
      Cmd(
        "CMD",
        "envsubst '$LOCATION $SERVER_NAME' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'")
    ),
    mappings in Docker ++= {
      val nginxTemplate     = "default.conf.template"
      val nginxTemplateFile = (target in Compile).value / nginxTemplate
      val nginxTemplateContent =
        s"""
           |server {
           |    listen 8080;
           |    server_name $${SERVER_NAME};
           |    port_in_redirect off;
           |    location $${LOCATION} {
           |        alias   /usr/share/nginx/html/;
           |        index  index.html;
           |    }
           |}
         """.stripMargin
      IO.write(nginxTemplateFile, nginxTemplateContent)
      MappingsHelper.directory((paradox in Compile).value) :+ (nginxTemplateFile -> nginxTemplate)
    },
    publishLocal := {
      publishLocal.value
      Def.taskDyn {
        if (!isSnapshot.value) Def.task { (publishLocal in Docker).value } else Def.task { () }
      }.value
    },
    publish := {
      publish.value
      Def.taskDyn {
        if (!isSnapshot.value) Def.task { (publish in Docker).value } else Def.task { () }
      }.value
    }
  )
}
