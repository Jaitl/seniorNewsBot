import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
  lazy val romeRss = "com.rometools" % "rome" % "1.12.0" exclude ("org.slf4j", "slf4j-api")
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  lazy val scalaConfig = "com.typesafe" % "config" % "1.3.3"
  lazy val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % "2.5.22"
  lazy val akkaStreamsTyped = "com.typesafe.akka" %% "akka-stream-typed" % "2.5.22"
  lazy val bot4s = "info.mukel" %% "telegrambot4s" % "3.0.16" excludeAll (
    ExclusionRule("com.typesafe.akka", "akka-actor_2.12"),
    ExclusionRule("com.typesafe.akka", "akka-stream_2.12"),
    ExclusionRule("com.typesafe.scala-logging", "scala-logging_2.12")
  )
  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.10.2"
}
