import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

name := "pokey"

version := "2.0-SNAPSHOT"

enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "2.2.4",
  "org.scaldi" %% "scaldi" % "0.5.8",
  "org.scaldi" %% "scaldi-akka" % "0.5.8",
  "org.scaldi" %% "scaldi-play" % "0.5.15",
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
// Scalariform

ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(SpacesAroundMultiImports, false)

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

//////////////////////////
// Docker Configuration

packageName in Docker := "web"
version in Docker := "latest"
dockerRepository := Some("registry.heroku.com/pokey")

dockerCommands := dockerCommands.value.filterNot {
  case ExecCmd("CMD", _*) => true
  case ExecCmd("ENTRYPOINT", _*) => true
  case _ => false
}

dockerCommands ++= Seq(
  ExecCmd("CMD", "sh", "-c", "bin/pokey -Dhttp.port=$PORT -Dpokey.tracking-id=$TRACKING_ID")
)
