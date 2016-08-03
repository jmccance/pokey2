import sbt._

object Dependencies {

  val JodaTime = "joda-time" % "joda-time" % "2.7"

  val Scalactic = "org.scalactic" %% "scalactic" % "2.2.4"

  object Scaldi {
    val Scaldi = "org.scaldi" %% "scaldi" % "0.5.6"
    val ScaldiAkka = "org.scaldi" %% "scaldi-akka" % "0.5.6"
    val ScaldiPlay = "org.scaldi" %% "scaldi-play" % "0.5.8"
  }

  val MockitoCore = "org.mockito" % "mockito-core" % "1.9.5"

  val Pegdown = "org.pegdown" % "pegdown" % "1.5.0"

  val AkkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.3.13"

  val ScalaTestPlusPlay = "org.scalatestplus" %% "play" % "1.4.0-M3"

  def testDependencies(moduleIds: ModuleID*): Seq[ModuleID] = moduleIds.map(_ % Test)
}
