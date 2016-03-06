resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)

// Play
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.0")

// Scoverage for code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

// Code formatting
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")
