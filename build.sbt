import Dependencies._

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
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
      romeRss,
      akkaActorTyped
    ),
    libraryDependencies ++= Seq(
      scalaTest
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
