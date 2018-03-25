package com.calvin.rpc


import com.calvin.rpc.account._
import cats.effect.IO
import scala.concurrent.Future


class AccountImpl(accountRepository: AccountRepository) extends AccountGrpc.Account {

  def getAccount(req: AccountRequest): Future[AccountResponse] = {
    val account = accountRepository.getAccount(req.accountId).unsafeRunSync().get
    Future.successful(AccountResponse(account.accountId, account.accountNumber, account.accountType, account.balance))
  }
}