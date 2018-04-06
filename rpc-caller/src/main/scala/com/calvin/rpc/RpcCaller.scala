package com.calvin.rpc


import java.nio.file.Paths

import io.grpc.ManagedChannelBuilder
import com.calvin.rpc.account._
//import com.calvin.rpc.hello._
import cats.effect.IO
import fs2.StreamApp
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object RpcCaller extends StreamApp[IO] with Http4sDsl[IO] {
  val channel = ManagedChannelBuilder.forAddress("0.0.0.0", 50051).usePlaintext(true).build

  val service = HttpService[IO] {
    case GET -> Root =>
      Ok("Welcome to the RPC Test Server!")
//    case GET -> Root / "hello-world" =>
//      for {
//        helloStub <- IO.pure(GreeterGrpc.stub(channel))
//        helloRequest <- IO.pure(HelloRequest(name = "World"))
//        reply = IO.pure(helloStub.sayHello(helloRequest))
//        fromFuture = IO.fromFuture(reply)
//        r <- Ok(fromFuture.map(_.toString))
//      } yield r
    case GET -> Root / "get-account" / accountId =>
      for {
        accountStub <- IO.pure(AccountGrpc.stub(channel))
        accountsRequest <- IO.pure(AccountRequest(accountId = accountId))
        reply = IO.pure(accountStub.getAccount(accountsRequest))
        fromFuture = IO.fromFuture(reply)
        r <- Ok(fromFuture.map(_.toString))
      } yield r
  }


  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(9000, "0.0.0.0")
      .mountService(service, "/")
      .serve
}
