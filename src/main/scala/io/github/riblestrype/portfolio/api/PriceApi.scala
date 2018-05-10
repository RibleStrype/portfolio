package io.github.riblestrype.portfolio.api

import cats.effect.IO
import io.circe.syntax._
import io.github.riblestrype.portfolio.domain.{DomainJson, SymbolPrice}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

object PriceApi extends DomainJson {

  def apply(prices: IO[List[SymbolPrice]]): HttpService[IO] = HttpService {
    case GET -> Root => Ok(prices.map(_.asJson))
  }
}
