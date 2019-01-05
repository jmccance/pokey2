resolvers ++= Seq(
  // "Sonatype OSS Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  // "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)

// Play
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.20")

// Scoverage for code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

// Code formatting
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
