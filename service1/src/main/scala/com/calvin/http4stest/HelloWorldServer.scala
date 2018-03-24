package com.calvin.http4stest

import com.calvin.http4stest.hello._
import cats.effect.IO
import fs2.{Stream, StreamApp}
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class GreeterImpl extends GreeterGrpc.Greeter {
  override def sayHello(req: HelloRequest) = {
    val reply = HelloReply(message = "Hello " + req.name)
    Future.successful(reply)
  }
}

object HelloWorldServer extends StreamApp[IO] with Http4sDsl[IO] {
  val service = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))

  }
  val rpcServer = IO.pure(new GreeterImpl)

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    for {
      stream <- BlazeBuilder[IO]
                  .enableHttp2(true)
                  .bindHttp(8090, "0.0.0.0")
                  .mountService(service, "/")
                  .serve concurrently (Stream.eval(rpcServer))
    } yield stream
}