package io.github.riblestrype.portfolio.api

import cats.effect.IO
import io.circe.syntax._
import io.github.riblestrype.portfolio.api.PathVars._
import io.github.riblestrype.portfolio.domain._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

object PortfolioApi extends DomainJson {

  def apply(portfolio: UserId => IO[Portfolio]): HttpService[IO] = HttpService {
    case GET -> Root / UserIdVar(owner) =>
      Ok(portfolio(owner).map(_.asJson))
  }
}
