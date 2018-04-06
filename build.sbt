val Http4sVersion = "0.18.7"
val Specs2Version = "4.0.3"
val LogbackVersion = "1.2.3"
val ProtobufVersion = "3.5.1"

val protobufSettings = PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value,
  fs2CodeGenerator -> (sourceManaged in Compile).value)

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
  .enablePlugins(DockerPlugin)
  .settings(
    organization := "mustang0168",
    name := "rpc-server",
    version := "1.2.0",
    scalaVersion := "2.12.5",
    protobufSettings,
    libraryDependencies ++= libraryDeps,
    dockerF,
    imageN

  )

lazy val rpc_caller = (project in file("rpc-caller"))
.enablePlugins(DockerPlugin)
  .settings(
    organization := "mustang0168",
    name := "rpc-caller",
    version := "1.2.0",
    scalaVersion := "2.12.5",
    protobufSettings,
    libraryDependencies ++= libraryDeps,
    dockerF,
    imageN
  )


lazy val root = project.in(file("."))

  .settings(
    name := "rpc-test-root",
    scalacOptions += "-Ypartial-unification"
  )
  .aggregate(
    `rpc_server`, `rpc_caller`
  )


val dockerF = dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName)
      .mkString(":") + ":" + jarTarget

  new Dockerfile {
    // Base image
    from("openjdk:8-jre")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}

val imageN = imageNames in docker := Seq(
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some(version.value)
  )
)
