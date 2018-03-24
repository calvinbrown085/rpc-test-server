val Http4sVersion = "0.18.3"
val Specs2Version = "4.0.3"
val LogbackVersion = "1.2.3"
val ProtobufVersion = "3.5.1"

lazy val root = (project in file("."))
  .settings(
    organization := "com.calvin",
    name := "http4s-test",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.5",
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % ProtobufVersion,
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.specs2"     %% "specs2-core"          % Specs2Version % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
    )
  )

