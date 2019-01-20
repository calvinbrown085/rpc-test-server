package com.calvin.rpc


import cats.implicits._
import cats.effect.Sync
import com.calvin.rpc.httptest._

class EchoService[F[_]: Sync]()(implicit F: Sync[F]) extends EchoServiceFs2Grpc[F] {

  def echo(request: com.calvin.rpc.httptest.EchoMessage, clientHeaders: _root_.io.grpc.Metadata): F[com.calvin.rpc.httptest.EchoMessage] = {
    F.pure(request)
  }
}

