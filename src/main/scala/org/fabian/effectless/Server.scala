package org.fabian.effectless

import cats.effect.IO
import config.Config
import fs2.{ Stream, StreamApp }
import fs2.StreamApp.ExitCode
import org.fabian.effectless.db.Database
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.fabian.effectless.world.tweet.{ TodoHttpEndpoint, TodoRepository }

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends StreamApp[IO] with Http4sDsl[IO] {

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    for {
      config     <- Stream.eval(Config.load())
      transactor <- Stream.eval(Database.transactor(config.database))
      _          <- Stream.eval(Database.initialize(transactor))
      exitCode <- BlazeBuilder[IO]
        .bindHttp(config.server.port, config.server.host)
        .mountService(new TodoHttpEndpoint(new TodoRepository(transactor)).service, "/")
        .serve
    } yield exitCode

}
