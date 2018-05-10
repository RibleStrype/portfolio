val Http4sVersion = "0.18.7"
val Specs2Version = "4.0.2"
val LogbackVersion = "1.2.3"
val DoobieVersion = "0.5.0-M14"
val CirceVersion = "0.9.1"
val PureConfigVersion = "0.9.1"

lazy val root = (project in file("."))
  .settings(
    organization := "io.github.riblestrype",
    name := "portfolio",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    scalacOptions += "-Ypartial-unification",
    test in assembly := {},
    mainClass in assembly := Some("io.github.riblestrype.portfolio.PortfolioServer"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-async-http-client" % Http4sVersion,

      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-literal" % CirceVersion,

      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,

      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,

      "org.specs2" %% "specs2-core" % Specs2Version % "test"
    )
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.discard
  case x                                                    =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

