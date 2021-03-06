import com.typesafe.sbt.packager.docker._

ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "io.github.kirill5k"

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  publish / skip := true
)

lazy val docker = Seq(
  packageName := moduleName.value,
  version := version.value,
  maintainer := "immotional@aol.com",
  dockerBaseImage := "adoptopenjdk/openjdk15-openj9:alpine-jre",
  dockerUpdateLatest := true,
  makeBatScripts := List(),
  dockerCommands := {
    val commands         = dockerCommands.value
    val (stage0, stage1) = commands.span(_ != DockerStageBreak)
    val (before, after)      = stage1.splitAt(4)
    val installBash = Cmd("RUN", "apk update && apk upgrade && apk add bash")
    stage0 ++ before ++ List(installBash) ++ after
  }
)

lazy val root = (project in file("."))
  .settings(noPublish)
  .settings(
    name := "telegram-bot",
  )
  .aggregate(core)

lazy val core = (project in file("core"))
  .enablePlugins(JavaAppPackaging, JavaAgent, DockerPlugin)
  .settings(docker)
  .settings(
    name := "telegram-bot-core",
    moduleName := "telegram-bot-core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test
  )

