package io.github.riblestrype.portfolio.api

import io.github.riblestrype.portfolio.domain.{Symbol, UserId}

private[api] object PathVars {

  object UserIdVar {
    def unapply(arg: String): Option[UserId] =
      Some(UserId(arg))
  }

  object SymbolVar {
    def unapply(arg: String): Option[Symbol] =
      Some(Symbol(arg))
  }
}
