package io.github.riblestrype.portfolio

import cats.effect.IO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.{Stream, StreamApp}
import io.github.riblestrype.portfolio.ExecutionContexts.dbIO
import io.github.riblestrype.portfolio.api.{BalanceApi, PortfolioApi, PriceApi}
import io.github.riblestrype.portfolio.domain._
import org.http4s.server.blaze.BlazeBuilder
import pureconfig.loadConfigOrThrow

object PortfolioServer extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  private def transactor(config: DBConfig) = HikariTransactor.stream[IO](
    driverClassName = "org.postgresql.Driver", // driver classname
    url = s"jdbc:postgresql:${ config.name }", // connect URL (driver-specific)
    user = config.user,
    pass = config.pass
  ) evalMap { xa =>
    xa.configure(hx => IO(hx.setAutoCommit(false))).map(_ => xa)
  }

  private def webServer(xa: Transactor[IO]): Stream[IO, StreamApp.ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(BalanceApi(balance(xa), saveBalance(xa), deleteBalance(xa)), "/balance")
      .mountService(PortfolioApi(portfolio(xa)), "/portfolio")
      .mountService(PriceApi(prices(xa)), "/prices")
      .serve

  override def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] =
    Stream.eval(IO(loadConfigOrThrow[DBConfig]("db")))
      .flatMap(transactor)
      .flatMap { xa =>
        webServer(xa).concurrently(PriceSync(xa))
      }

  private def prices(xa: Transactor[IO]): IO[List[SymbolPrice]] =
    dbIO(DB.symbolPrices.transact(xa))

  private def balance(xa: Transactor[IO])(owner: UserId): IO[List[Balance]] =
    dbIO(DB.balance(owner).transact(xa))

  private def saveBalance(xa: Transactor[IO])(owner: UserId, symbol: Symbol, amount: Amount): IO[_] =
    dbIO(DB.saveBalance(owner, symbol, amount).transact(xa))

  private def deleteBalance(xa: Transactor[IO])(owner: UserId, symbol: Symbol): IO[_] =
    dbIO(DB.deleteBalance(owner, symbol).transact(xa))

  private def portfolio(xa: Transactor[IO])(owner: UserId): IO[Portfolio] =
    dbIO(DB.portfolio(owner).transact(xa))

  private case class DBConfig(name: String, user: String, pass: String)
}
