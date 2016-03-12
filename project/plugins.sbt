resolvers ++= Seq(
  "Sonatype OSS Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)

// Play
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.2")

// Scoverage for code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

// Code formatting
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")
