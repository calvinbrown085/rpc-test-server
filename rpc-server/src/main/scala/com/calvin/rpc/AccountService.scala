package com.calvin.rpc


import com.calvin.rpc.account._
import cats.implicits._
import cats.effect.Sync
import cats.effect.IO
import scala.concurrent.Future


class AccountImpl[F[_]: Sync](accountRepository: AccountRepository[F]) extends AccountFs2Grpc[F] {

  def getAccount(request: com.calvin.rpc.account.AccountRequest, clientHeaders: io.grpc.Metadata): F[AccountResponse] = {
    for {
      account <- accountRepository.getAccount(request.accountId)
      acct = account.get
    } yield AccountResponse(acct.accountId, acct.accountNumber, acct.accountType, acct.balance)
  }
}
