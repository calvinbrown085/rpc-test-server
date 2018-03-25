package com.calvin.rpc

import com.calvin.rpc.account._
import com.calvin.rpc.hello._
import cats.effect.IO
import fs2.{Stream, StreamApp}
import io.circe._
import java.nio.file.Paths

import io.grpc.{Server, ServerBuilder}


import org.http4s._
import org.http4s.circe._
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

class GreeterImpl extends GreeterGrpc.Greeter {
  override def sayHello(req: HelloRequest) = {
    val reply = HelloReply(message = "Hello " + req.name)
    Future.successful(reply)
  }
}


object RPCServer extends StreamApp[IO] with Http4sDsl[IO] {
  val service = HttpService[IO] {
    case GET -> Root =>
      Ok("Welcome to the RPC Test Server!")

  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    for {
      accountRepo <- Stream(AccountRepository.create())
      grpcServer <- Stream(IO.pure(ServerBuilder.forPort(50051).addService(GreeterGrpc.bindService(new GreeterImpl, ExecutionContext.global))
                                                               .addService(AccountGrpc.bindService(new AccountImpl(accountRepo), ExecutionContext.global)).build.start))
      stream <- BlazeBuilder[IO]
                  .bindHttp(8080, "0.0.0.0")
                  .mountService(service, "/")
                  .serve concurrently Stream.eval(grpcServer)
    } yield stream
}
