package io.github.riblestrype.portfolio

import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

object domain {
  case class Symbol(value: String) extends AnyVal
  case class Price(value: BigDecimal) extends AnyVal
  case class Amount(value: BigDecimal) extends AnyVal {
    def +(that: Amount): Amount =
      Amount(value + that.value)
  }
  case class UserId(value: String) extends AnyVal

  case class SymbolPrice(symbol: Symbol, priceUSD: Price, priceBTC: Price, rank: Int)
  case class Balance(symbol: Symbol, amount: Amount)
  case class PricedBalance(symbol: Symbol, amount: Amount, priceUSD: Option[Price]) {
    def worthUSD: Option[Amount] =
      priceUSD.map(p => Amount(amount.value * p.value))
  }
  case class Portfolio(balances: List[PricedBalance]) {
    def worthUSD: Approximation[Amount] =
      balances.map(_.worthUSD).foldLeft(Approximation.exactly(Amount(0))) {
        case (Approximation.Exactly(totalAmount), None)         => Approximation.AtLeast(totalAmount)
        case (Approximation.Exactly(totalAmount), Some(amount)) => Approximation.Exactly(totalAmount + amount)
        case (Approximation.AtLeast(totalAmount), None)         => Approximation.AtLeast(totalAmount)
        case (Approximation.AtLeast(totalAmount), Some(amount)) => Approximation.AtLeast(totalAmount + amount)
      }
  }

  sealed trait Approximation[A]
  object Approximation {
    def exactly[A](value: A): Approximation[A] = Exactly(value)
    final case class Exactly[A](value: A) extends Approximation[A]
    final case class AtLeast[A](value: A) extends Approximation[A]
  }

  trait DomainJson {
    implicit val symbolEncoder = Encoder.encodeString.contramap[Symbol](_.value)
    implicit val symbolDecoder = Decoder.decodeString.map(Symbol)

    implicit val priceEncoder = Encoder.encodeBigDecimal.contramap[Price](_.value)
    implicit val priceDecoder = Decoder.decodeBigDecimal.map(Price)

    implicit val amountEncoder = Encoder.encodeBigDecimal.contramap[Amount](_.value)
    implicit val amountDecoder = Decoder.decodeBigDecimal.map(Amount)

    implicit val userIdEncoder = Encoder.encodeString.contramap[UserId](_.value)
    implicit val userIdDecoder = Decoder.decodeString.map(UserId)

    implicit val symbolPriceEncoder = deriveEncoder[SymbolPrice]
    implicit val symbolPriceDecoder = deriveDecoder[SymbolPrice]

    implicit val balanceEncoder = deriveEncoder[Balance]
    implicit val balanceDecoder = deriveDecoder[Balance]

    implicit val pricedBalanceEncoder = new Encoder[PricedBalance] {
      override def apply(pb: PricedBalance): Json = Json.obj(
        ("symbol", pb.symbol.asJson),
        ("amount", pb.amount.asJson),
        ("price_usd", pb.priceUSD.asJson),
        ("worth_usd", pb.worthUSD.asJson)
      )
    }

    implicit def approximationEncoder[A: Encoder] =
      deriveEncoder[Approximation[A]]

    implicit val portfolioEncoder = new Encoder[Portfolio] {
      override def apply(p: Portfolio): Json = Json.obj(
        ("balances", p.balances.asJson),
        ("worthUSD", p.worthUSD.asJson)
      )
    }
  }
}
