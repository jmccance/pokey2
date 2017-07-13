import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

name := "pokey"

version := "2.0-SNAPSHOT"

enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.2",
  "org.scalactic" %% "scalactic" % "3.0.1",

  // Test dependencies

  "org.mockito" % "mockito-core" % "1.9.5" % Test,
  "org.pegdown" % "pegdown" % "1.5.0" % Test,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test
)

scalacOptions in (Compile, compile) ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-unchecked",
//  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

////////////////////////
// Scalariform

ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(SpacesAroundMultiImports, false)

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
