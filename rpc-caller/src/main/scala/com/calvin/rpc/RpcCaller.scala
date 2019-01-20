package com.calvin.rpc


import cats.implicits._
import fs2.Stream
import io.grpc.{ManagedChannel, ManagedChannelBuilder, Metadata}
import cats.effect.IO
import com.calvin.rpc.httptest._
import com.calvin.rpc.account._
import fs2.StreamApp
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.lyranthe.fs2_grpc.java_runtime.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object RpcCaller extends StreamApp[IO] with Http4sDsl[IO] {

  def service(accountFs2Grpc: AccountFs2Grpc[IO], echoServiceFs2Grpc: EchoServiceFs2Grpc[IO]) = HttpService[IO] {
    case GET -> Root =>
      Ok("Welcome to the RPC Test Server!")

    case GET -> Root / "get-account" / accountId =>
      for {
        accountsRequest <- IO.pure(AccountRequest(accountId = accountId))
        reply <- accountFs2Grpc.getAccount(accountsRequest, new Metadata())
        r <- Ok(reply.toString)
      } yield r

    case GET -> Root / "echo" / message =>
      for {
        reply <- echoServiceFs2Grpc.echo(EchoMessage(value = message), new Metadata())
        r <- Ok(reply.toString)
      } yield r
  }
  


  def stream(args: List[String], requestShutdown: IO[Unit]) =
    for {
      channel <- ManagedChannelBuilder.forAddress("0.0.0.0", 9999).usePlaintext().stream[IO]
      accountsGrpc = AccountFs2Grpc.stub[IO](channel)
      echoServiceGrpc = EchoServiceFs2Grpc.stub[IO](channel)
      s <- BlazeBuilder[IO]
          .bindHttp(9000, "0.0.0.0")
          .mountService(service(accountsGrpc, echoServiceGrpc), "/")
          .serve
    } yield s

}
