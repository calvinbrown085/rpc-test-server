val Http4sVersion = "0.18.3"
val Specs2Version = "4.0.3"
val LogbackVersion = "1.2.3"
val ProtobufVersion = "3.5.1"

val protobufSettings = PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value)

val libraryDeps =  Seq(
  "com.google.protobuf" % "protobuf-java" % ProtobufVersion,
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "org.specs2"     %% "specs2-core"          % Specs2Version % "test",
  "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
  "io.grpc" % "grpc-stub" % "1.10.0",
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)

lazy val rpc_server = (project in file("rpc-server"))
  .settings(
    organization := "com.calvin",
    name := "rpc-server",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.5",
    protobufSettings,
    libraryDependencies ++= libraryDeps
  )

lazy val rpc_caller = (project in file("rpc-caller"))
  .settings(
    organization := "com.calvin",
    name := "rpc-caller",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.5",
    protobufSettings,
    libraryDependencies ++= libraryDeps
  )


lazy val root = project.in(file("."))
  .settings(name := "rpc-test-root")
  .aggregate(
    `rpc_server`, `rpc_caller`
  )
