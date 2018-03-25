package com.calvin.rpc

import cats.effect.IO

trait AccountRepository {
  def getAccount(accountId: String): IO[Option[Account]]
}

object AccountRepository {
  val db: Map[String, Account] = Map("1" -> Account("1", "1234", "Checking", 1000), "2" -> Account("2", "5678", "Savings", 2000))

  def create() = new AccountRepository {
    def getAccount(accountId: String): IO[Option[Account]] = IO.pure(db.get(accountId))
  }
}