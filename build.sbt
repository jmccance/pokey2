import ScoverageSbtPlugin._

name := """pokey2"""

version := "2.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.7",
  "org.scalactic" %% "scalactic" % "2.2.4",
  "org.scaldi" %% "scaldi" % "0.5.3",
  "org.scaldi" %% "scaldi-akka" % "0.5.3",
  "org.scaldi" %% "scaldi-play" % "0.5.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test",
  "org.scalatestplus" %% "play" % "1.2.0" % "test"
)

ScoverageKeys.coverageMinimum := 80

ScoverageKeys.coverageFailOnMinimum := true

ScoverageKeys.coverageExcludedPackages := Seq(
  "<empty>",
  ".*\\.controller\\.javascript",
  ".*\\.controller\\.ref"
).mkString(";")
