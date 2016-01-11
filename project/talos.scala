import sbt.Keys._
import sbt._

object Resolvers {
  val typesafe = Seq(
    "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/")
  val resolversList = typesafe
}

object Dependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % Test

  val scalaReflect = "org.scala-lang" % "scala-reflect" % "2.11.7"
  val scalaCompiler = "org.scala-lang" % "scala-compiler" % "2.11.7"
}

object BuildSettings {
  val buildVersion = "0.0.1-SNAPSHOT"

  val buildSettings = Defaults.coreDefaultSettings ++ Seq(
    organization := "com.danieluzunu",
    version := buildVersion,
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-target:jvm-1.7"))
}

object TalosBuild extends Build {

  import BuildSettings._
  import Resolvers._
  import Dependencies._

  lazy val root = Project(
      "talos-root",
      file("."),
      settings = buildSettings
  ).aggregate(core, example)

  lazy val core = Project(
    "talos",
    file("core"),
    settings = buildSettings ++ Seq(
      resolvers := resolversList,
      libraryDependencies ++= Seq(scalaTest, scalaCompiler, scalaReflect))
  )

  lazy val example = Project(
    "talos-example",
    file("example"),
    settings = buildSettings ++ Seq(
      resolvers := resolversList,
      libraryDependencies ++= Seq())
  ).dependsOn(core)
}
