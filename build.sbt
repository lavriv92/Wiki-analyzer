ThisBuild / scalaVersion := "2.13.6"
ThisBuild / organization := "WikiAnalyzer"

val AkkaVersion = "2.6.17"
val AkkaHttpVersion = "10.2.7"

lazy val root = (project in file("."))
  .settings(
    name := "WikiAnalyzer",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-sse" % "3.0.4",
      "org.scalatest" %% "scalatest" % "3.2.7" % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.8.0"
    )
  )
