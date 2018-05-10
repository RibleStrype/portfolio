package io.github.riblestrype.portfolio

import cats.effect._
import cats.syntax.all._
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.{Scheduler, Stream}
import io.circe.{Decoder, HCursor}
import io.github.riblestrype.portfolio.ExecutionContexts.dbIO
import io.github.riblestrype.portfolio.domain.{Price, Symbol, SymbolPrice}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.client.asynchttpclient.AsyncHttpClient
import org.log4s.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

object PriceSync {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val CoinMarketCapUrl = "https://api.coinmarketcap.com/v1/ticker/"

  private val logger = new Logger(LoggerFactory.getLogger(getClass.getName))

  def apply(xa: Transactor[IO]): Stream[IO, Unit] =
    httpClient.flatMap { client =>
      Scheduler[IO](1).flatMap { scheduler =>
        (fetch(client).evalMap(saveSymbolPrice(xa)) ++ scheduler.sleep_[IO](10.seconds)).repeat
      }
    }

  private def saveSymbolPrice(xa: Transactor[IO])(sp: SymbolPrice): IO[Unit] =
    for {
      _ <- IO(logger.debug(s"Saving: $sp"))
      _ <- dbIO(DB.saveSymbolPrice(sp).transact(xa))
    } yield ()

  implicit private val symbolPriceDecoder: Decoder[SymbolPrice] =
    (c: HCursor) => for {
      symbol <- c.downField("symbol").as[String]
      priceUsd <- c.downField("price_usd").as[BigDecimal]
      priceBtc <- c.downField("price_btc").as[BigDecimal]
      rank <- c.downField("rank").as[Int]
    } yield SymbolPrice(Symbol(symbol), Price(priceUsd), Price(priceBtc), rank)

  implicit private val symbolPriceIODecoder: EntityDecoder[IO, List[SymbolPrice]] =
    jsonOf[IO, List[SymbolPrice]]

  private def fetch(client: Client[IO]): Stream[IO, SymbolPrice] = {
    val fetcher = client.expect[List[SymbolPrice]](CoinMarketCapUrl).handleError { e =>
      logger.warn(e)("price fetch failed")
      List.empty
    }
    Stream
      .eval(fetcher)
      .flatMap(Stream.emits(_))
  }

  private def httpClient =
    Stream.bracket(Effect[IO].delay(AsyncHttpClient[IO]()))(c => Stream.emit(c), _.shutdown)
}
