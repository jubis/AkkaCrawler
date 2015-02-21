import NativePackagerKeys._

packageArchetype.akka_application

name := """akkatest"""

version := "1.0"

scalaVersion := "2.11.5"

//mainClass in Compile := Some("me.matiass.wikicrawler.MyKernel")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-kernel" % "2.3.9",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
)
