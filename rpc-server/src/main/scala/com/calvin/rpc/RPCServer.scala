package com.calvin.rpc

import com.calvin.rpc.account._
import cats.effect._
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



object RPCServer extends StreamApp[IO] with Http4sDsl[IO] {
  val service = HttpService[IO] {
    case GET -> Root =>
      Ok("Welcome to the RPC Test Server!")

  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    for {
      accountRepo <- Stream(AccountRepository.create[IO]())
      grpcServer = Stream(IO.pure(ServerBuilder.forPort(50051).addService(AccountFs2Grpc.bindService[IO](new AccountImpl(accountRepo))(Effect[IO], ExecutionContext.global)).build.start))
      stream <- BlazeBuilder[IO]
                  .bindHttp(8080, "0.0.0.0")
                  .mountService(service, "/")
                  .serve concurrently grpcServer
    } yield stream
}
