val Http4sVersion = "0.18.16"
val Specs2Version = "4.0.3"
val LogbackVersion = "1.2.3"

lazy val protobuf =
  project
      .in(file("protobuf"))
  .enablePlugins(Fs2Grpc)
  .settings(
    PB.generate in Compile := (PB.generate in Compile).dependsOn(extractProtos).value,
    PB.protoSources in Compile += sourceManaged.value / "googleapis-master",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"))


val libraryDeps =  Seq(
  "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-prometheus-server-metrics"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "org.specs2"     %% "specs2-core"          % Specs2Version % "test",
  "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
)

lazy val extractProtos = Def.task {
  if (!(sourceManaged.value / "googleapis-master").exists) {
    val zipUrl = "https://github.com/googleapis/googleapis/archive/master.zip"
    IO.unzipURL(
      from=url(zipUrl),
      filter=(
          "googleapis-master/google/api/*" |
          "googleapis-master/google/logging/*" |
          "googleapis-master/google/longrunning/*" |
          "googleapis-master/google/rpc/*" |
          "googleapis-master/google/type/*"
        ),
      toDirectory=sourceManaged.value)
  }
}

lazy val rpc_server = (project in file("rpc-server"))
  .enablePlugins(DockerPlugin)
  .dependsOn(protobuf)
  .settings(
    organization := "mustang0168",
    name := "rpc-server",
    version := "1.2.0",
    scalaVersion := "2.12.5",
    libraryDependencies ++= libraryDeps,
    dockerF,
    imageN

  )

lazy val rpc_caller = (project in file("rpc-caller"))
.enablePlugins(DockerPlugin)
  .dependsOn(protobuf)
  .settings(
    organization := "mustang0168",
    name := "rpc-caller",
    version := "1.2.0",
    scalaVersion := "2.12.5",
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
    `rpc_server`, `rpc_caller`, `protobuf`
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
