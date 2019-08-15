import Dependencies._
import com.typesafe.sbt.packager.docker._

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "1.0.7"
ThisBuild / organization := "com.github.jaitl"
ThisBuild / organizationName := "jaitl.pro"

lazy val root = (project in file("."))
  .settings(
    name := "seniorNewsBot",
    scapegoatVersion in ThisBuild := "1.3.8",
    scapegoatDisabledInspections := Seq("FinalModifierOnCaseClass"),
    conflictManager := ConflictManager.strict
  )
  .settings(
    libraryDependencies ++= Seq(
      logback,
      scalaLogging,
      scalaConfig,
      pureconfig,
      romeRss,
      akkaActorTyped,
      akkaStreamsTyped,
      bot4s,
      mapDb
    ),
    libraryDependencies ++= Seq(
      scalaTest
    )
  )
  .enablePlugins(JavaAppPackaging)
  .settings(
    mainClass in Compile := Some("com.github.jaitl.seniornews.Application")
  )
  .settings(
    dockerRepository in Docker := Some("jaitl")
  )
  .settings(
    javaOptions in Universal ++= Seq(
      // -J params will be added as jvm parameters
      "-J-Xmx128m",
      "-J-Xms32m"
    )
  )
  .settings(
    scalacOptions := Seq(
      "-encoding",
      "UTF-8", // source files are in UTF-8
      "-deprecation", // warn about use of deprecated APIs
      "-unchecked", // warn about unchecked type parameters
      "-feature", // warn about misused language features
      "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
      "-Xlint", // enable handy linter warnings
      "-Xfatal-warnings", // turn compiler warnings into errors
      "-Ypartial-unification" // allow the compiler to unify type constructors of different arities
    )
  )
