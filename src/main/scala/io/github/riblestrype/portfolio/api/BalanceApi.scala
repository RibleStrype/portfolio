package io.github.riblestrype.portfolio.api

import cats.effect.IO
import io.circe.syntax._
import io.github.riblestrype.portfolio.api.PathVars._
import io.github.riblestrype.portfolio.domain.{Amount, Balance, DomainJson, Symbol, UserId}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

object BalanceApi extends DomainJson {

  private implicit val amountIODecoder: EntityDecoder[IO, Amount] = jsonOf[IO, Amount]

  def apply(
    balance: UserId => IO[List[Balance]],
    saveBalance: (UserId, Symbol, Amount) => IO[_],
    deleteBalance: (UserId, Symbol) => IO[_]
  ): HttpService[IO] = HttpService {
    case GET -> Root / UserIdVar(owner) =>
      Ok(balance(owner).map(_.asJson))

    case req@POST -> Root / UserIdVar(owner) / SymbolVar(symbol) =>
      for {
        amount <- req.as[Amount]
        _ <- saveBalance(owner, symbol, amount)
        resp <- Ok(())
      } yield resp

    case DELETE -> Root / UserIdVar(owner) / SymbolVar(symbol) =>
      Ok(deleteBalance(owner, symbol).map(_ => ()))
  }
}
