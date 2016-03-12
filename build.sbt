

name := "pokey"

version := "2.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.7",
  "org.scalactic" %% "scalactic" % "2.2.4",
  "org.scaldi" %% "scaldi" % "0.5.6",
  "org.scaldi" %% "scaldi-akka" % "0.5.6",
  "org.scaldi" %% "scaldi-play" % "0.5.8",
  "org.mockito" % "mockito-core" % "1.9.5" % Test,
  "org.pegdown" % "pegdown" % "1.5.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.3.13" % Test,
  "org.scalatestplus" %% "play" % "1.4.0-M3" % Test
)

scalacOptions in (Compile, compile) ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

////////////////////////
// Play Configuration

routesGenerator := InjectedRoutesGenerator

////////////////////////
// Test Configuration

javaOptions in Test += "-Dconfig.resource=test.conf"

testOptions in Test += Tests.Argument("-h", "target/test-reports-html")

coverageMinimum := 90

coverageFailOnMinimum := true

coverageExcludedPackages := Seq(
  "<empty>",
  ".*\\.controller\\.javascript",
  ".*\\.controller\\.ref",
  "router.*"
).mkString(";")

