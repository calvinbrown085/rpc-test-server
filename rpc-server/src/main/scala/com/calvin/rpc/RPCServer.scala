package com.calvin.rpc

import com.calvin.rpc.httptest._
import com.calvin.rpc.account._
import cats.effect._
import cats.effect.IO
import fs2.{Stream, StreamApp}
import io.circe._
import java.nio.file.Paths

import io.grpc.{Server, ServerBuilder, ServerServiceDefinition}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import org.lyranthe.fs2_grpc.java_runtime.implicits._



object RPCServer extends StreamApp[IO] with Http4sDsl[IO] {
  val service = HttpService[IO] {
    case GET -> Root =>
      Ok("Welcome to the RPC Test Server!")

  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    for {
      accountRepo <- Stream(AccountRepository.create[IO]())
      echoService = EchoServiceFs2Grpc.bindService[IO](new EchoService[IO]())(Effect[IO], ExecutionContext.Implicits.global)
      accountGrpc = AccountFs2Grpc.bindService[IO](new AccountImpl(accountRepo))(Effect[IO], ExecutionContext.Implicits.global)
      grpcServer = ServerBuilder.forPort(9999).addService(accountGrpc).addService(echoService).stream[IO].evalMap(server => IO(server.start()))
          .evalMap(_ => IO.never)
      stream <- BlazeBuilder[IO]
                  .bindHttp(8080, "0.0.0.0")
                  .mountService(service, "/")
                  .serve concurrently grpcServer
    } yield stream
}
