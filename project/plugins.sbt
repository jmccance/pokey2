resolvers ++= Seq(
  "Sonatype OSS Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)

// Play
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6")

// Scoverage for code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

// Scala JS
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.11")

// Code formatting
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// Docker
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")

