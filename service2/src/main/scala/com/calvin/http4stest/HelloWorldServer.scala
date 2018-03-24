package com.calvin.http4stest


import java.nio.file.Paths

import io.grpc.ManagedChannelBuilder
import com.calvin.http4stest.hello._
import cats.effect.IO
import fs2.StreamApp
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object HelloWorldServer extends StreamApp[IO] with Http4sDsl[IO] {
  val channel = ManagedChannelBuilder.forAddress("0.0.0.0", 50051).usePlaintext(true).build
  val request = HelloRequest(name = "World")
  val service = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
    case GET -> Root / "rpc-test" =>
      val blockingStub = GreeterGrpc.blockingStub(channel)
      val reply: HelloReply = blockingStub.sayHello(request)
      println(reply)
      Ok(reply.toString)

  }

  val keypath: String = Paths.get("service1/src/main/resources/server.jks").toAbsolutePath.toString

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
//      .withSSL(StoreInfo(keypath, "password"), keyManagerPassword = "secure")
//      .enableHttp2(true)
      .bindHttp(9000, "0.0.0.0")
      .mountService(service, "/")
      .serve
}
