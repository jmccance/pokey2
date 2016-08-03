import Dependencies._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import com.typesafe.sbt.packager.docker._

import scalariform.formatter.preferences._

organization in ThisBuild := "net.jmccance"
version in ThisBuild := "2.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.11.8"

// TODO: Once the Scala.js client exists, rename it to just "client".
lazy val clientSJS =
  (project in file("clientSJS"))
    .enablePlugins(ScalaJSPlugin)

lazy val core = crossProject in file("core")
lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val web =
  (project in file("web"))
    .enablePlugins(PlayScala)
    .disablePlugins(PlayLayoutPlugin)
    .dependsOn(coreJVM)
    .settings(
      name := "pokey-web",

      libraryDependencies ++= Seq(
        JodaTime,
        Scalactic,
        Scaldi.Scaldi,
        Scaldi.ScaldiAkka,
        Scaldi.ScaldiPlay
      ),

      libraryDependencies ++= testDependencies(
        MockitoCore,
        Pegdown,
        AkkaTestKit,
        ScalaTestPlusPlay
      ),

      scalacOptions in(Compile, compile) ++= Seq(
        "-deprecation",
        "-encoding", "UTF-8", // yes, this is 2 args
        "-feature",
        "-unchecked",
        "-Xfatal-warnings",
        "-Xlint",
        "-Yno-adapted-args",
        "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard",
        "-Xfuture"
      ),


      // Play Config ////

      routesGenerator := InjectedRoutesGenerator,

      // Scalariform ////

      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(SpacesAroundMultiImports, false),

      // Test Configuration ////

      javaOptions in Test += "-Dconfig.resource=test.conf",

      testOptions in Test += Tests.Argument("-h", "target/test-reports-html"),

      coverageMinimum := 90,

      coverageFailOnMinimum := true,

      coverageExcludedPackages := Seq(
        "<empty>",
        ".*\\.controller\\.javascript",
        ".*\\.controller\\.ref",
        "router.*"
      ).mkString(";"),

      // Docker Configuration ////

      packageName in Docker := "web",
      version in Docker := "latest",
      dockerRepository := Some("registry.heroku.com/pokey"),

      dockerCommands := dockerCommands.value.filterNot {
        case ExecCmd("CMD", _*) => true
        case ExecCmd("ENTRYPOINT", _*) => true
        case _ => false
      },

      dockerCommands ++= Seq(
        ExecCmd("CMD", "sh", "-c", "bin/pokey -Dhttp.port=$PORT")
      )
    )
