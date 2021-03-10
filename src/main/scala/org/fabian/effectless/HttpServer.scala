package org.fabian.effectless

import cats.effect.IO
import cats.effect.{ ConcurrentEffect, Timer }
import org.http4s.server.blaze.BlazeBuilder
import fs2.Stream
import scala.concurrent.ExecutionContext.global
import org.http4s.server.blaze.BlazeServerBuilder

import org.fabian.effectless.config.Config
import org.fabian.effectless.db.Database
import org.fabian.effectless.domain.todo.{ TodoHttpEndpoint, TodoRepository }

object HttpServer {
  def stream(): Stream[IO, Nothing] =
    for {
      config     <- Stream.eval(Config.load())
      transactor <- Stream.eval(Database.transactor(config.database))
      _          <- Stream.eval(Database.initialize(transactor))
      exitCode <- BlazeServerBuilder[IO](global)
        .bindHttp(config.server.port, config.server.host)
        .mountService(new TodoHttpEndpoint(new TodoRepository(transactor)).service, "/")
        .serve
    } yield exitCode
}
