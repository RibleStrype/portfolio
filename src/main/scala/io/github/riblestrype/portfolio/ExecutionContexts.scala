package io.github.riblestrype.portfolio

import java.util.concurrent.Executors

import cats.effect.IO

import scala.concurrent.ExecutionContext

object ExecutionContexts {
  val Main = ExecutionContext.global
  val BlockingDB = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def dbIO[A](io: IO[A]): IO[A] = for {
    _ <- IO.shift(BlockingDB)
    a <- io
    _ <- IO.shift(Main)
  } yield a
}
