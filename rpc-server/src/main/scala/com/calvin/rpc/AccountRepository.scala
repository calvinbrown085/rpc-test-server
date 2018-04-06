package com.calvin.rpc

import cats.effect.Sync
import cats.effect.IO

trait AccountRepository[F[_]] {
  def getAccount(accountId: String)(implicit F: Sync[F]): F[Option[Account]]
}

object AccountRepository {
  val db: Map[String, Account] = Map("1" -> Account("1", "1234", "Checking", 1000), "2" -> Account("2", "5678", "Savings", 2000))

  def create[F[_]]() = new AccountRepository[F] {
    def getAccount(accountId: String)(implicit F: Sync[F]): F[Option[Account]] = F.pure(db.get(accountId))
  }
}
