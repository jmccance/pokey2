name := """pokey2"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.7",
  "org.scaldi" %% "scaldi" % "0.5.3",
  "org.scaldi" %% "scaldi-akka" % "0.5.3",
  "org.scaldi" %% "scaldi-play" % "0.5.3"
)
