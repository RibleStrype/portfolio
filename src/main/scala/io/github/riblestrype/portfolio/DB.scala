package io.github.riblestrype.portfolio

import doobie._
import doobie.implicits._
import io.github.riblestrype.portfolio.domain._

object DB {

  def saveSymbolPrice(sp: SymbolPrice): ConnectionIO[_] =
    sql"""
      INSERT INTO symbol_price(symbol, price_usd, price_btc, rank)
      VALUES (${ sp.symbol.value }, ${ sp.priceUSD.value }, ${ sp.priceBTC.value }, ${ sp.rank })
      ON CONFLICT (symbol) DO UPDATE SET price_usd = EXCLUDED.price_usd, price_btc = EXCLUDED.price_btc
      """.update.run

  val symbolPrices: ConnectionIO[List[SymbolPrice]] =
    sql"SELECT * FROM symbol_price ORDER BY rank".query[SymbolPrice].list

  def balance(owner: UserId): ConnectionIO[List[Balance]] =
    sql"SELECT symbol, amount FROM balance WHERE owner = ${ owner.value }".query[Balance].list

  def saveBalance(owner: UserId, symbol: Symbol, amount: Amount): ConnectionIO[_] =
    sql"""
       INSERT INTO balance(owner, symbol, amount)
       VALUES (${ owner.value }, ${ symbol.value }, ${ amount.value })
       ON CONFLICT (owner, symbol) DO UPDATE SET amount = EXCLUDED.amount
      """.update.run

  def deleteBalance(owner: UserId, symbol: Symbol): ConnectionIO[_] =
    sql"DELETE FROM balance WHERE owner = ${ owner.value } AND symbol = ${ symbol.value }".update.run

  def portfolio(owner: UserId): ConnectionIO[Portfolio] =
    sql"""
        SELECT b.symbol, b.amount, sp.price_usd FROM balance b
        LEFT JOIN symbol_price sp ON b.symbol = sp.symbol
        WHERE b.owner = ${ owner.value }
      """.query[PricedBalance].list.map(Portfolio)
}
