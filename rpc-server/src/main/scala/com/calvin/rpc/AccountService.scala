package com.calvin.rpc


import com.calvin.rpc.account._
import cats.effect.Effect
import cats.effect.IO
import scala.concurrent.Future


class AccountImpl(accountRepository: AccountRepository) extends AccountFs2Grpc[IO] {

  def getAccount(request: com.calvin.rpc.account.AccountRequest, clientHeaders: io.grpc.Metadata): IO[AccountResponse] = {
    val account = accountRepository.getAccount(request.accountId).unsafeRunSync().get
    IO.pure(AccountResponse(account.accountId, account.accountNumber, account.accountType, account.balance))
  }
}
