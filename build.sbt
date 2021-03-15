// https://github.com/http4s/http4s.g8/tree/0.21/src/main/g8/src/main/scala/%24package__packaged%24

val Http4sVersion = "0.21.20"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"
val DoobieVersion = "0.9.0"
val H2Version = "1.4.192"
val CirceVersion = "0.13.0"
val FlywayVersion = "4.2.0"
val PureConfigVersion = "0.14.1"
val ScalaTestVersion = "3.2.6"
val ScalaMockVersion = "5.1.0"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    organization := "org.fabian",
    name := "google-home-backend",
    version := "0.0.2-SNAPSHOT",
    scalaVersion := "2.13.5",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion % "it,test",
      // Doobie
      "org.tpolecat"   %% "doobie-core"     % DoobieVersion,
      "org.tpolecat"   %% "doobie-hikari"   % DoobieVersion,
      "org.tpolecat"   %% "doobie-postgres" % "0.10.0",
      "com.h2database" % "h2"               % H2Version,
      "org.flywaydb"   % "flyway-core"      % FlywayVersion,
      // Circe
      "io.circe"      %% "circe-generic" % CirceVersion, // Optional for auto-derivation of JSON codecs
      "io.circe"      %% "circe-literal" % CirceVersion % "it,test", // Optional for string interpolation to JSON model
      "io.circe"      %% "circe-optics"  % CirceVersion % "it",
      "org.typelevel" %% "jawn-parser"   % "1.0.0", // depends de circe 0.13.0
      // Others
      "com.github.pureconfig" %% "pureconfig"         % PureConfigVersion,
      "ch.qos.logback"        % "logback-classic"     % LogbackVersion,
      "org.scalatest"         %% "scalatest"          % ScalaTestVersion % "it,test",
      "org.scalatest"         %% "scalatest-wordspec" % ScalaTestVersion % "it, test",
      "org.scalamock"         %% "scalamock"          % ScalaMockVersion % "test"
    )
  )
  .settings(
    fork in run := true,
    fork in Test := true,
    fork in IntegrationTest := true,
    fmtSettings
  )

lazy val fmtSettings =
  Seq(
    scalafmtOnCompile := true,
    scalafmtOnCompile.in(Sbt) := false,
    scalafmtVersion := "1.3.0"
  )
