package org.fabian.effectless

import cats.data.Kleisli
import cats.effect.{ ContextShift, ExitCode, IO, Timer }
import cats.implicits._
import doobie.hikari.HikariTransactor
import fs2.Stream
import org.fabian.effectless.config.AppConfig
import org.fabian.effectless.db.Database
import org.fabian.effectless.domain.health.HealthCheckHttpEndpoint
import org.fabian.effectless.domain.todo.{ TodoHttpEndpoint, TodoRepository }
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.{ Request, Response }

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

final class HttpServer(implicit executionContext: ExecutionContext) {

  private implicit val cs: ContextShift[IO] = IO.contextShift(executionContext)
  private implicit val timer: Timer[IO] = IO.timer(executionContext)

  private def buildRoutes(
    transactor: HikariTransactor[IO]
  ): Kleisli[IO, Request[IO], Response[IO]] =
    (
      new TodoHttpEndpoint(new TodoRepository(transactor)).service <+>
        new HealthCheckHttpEndpoint("health").service
    ).orNotFound

  def stream(): Stream[IO, ExitCode] =
    for {
      config     <- Stream.eval(AppConfig.load())
      transactor <- Stream.eval(Database.transactor(config.database))
      _          <- Stream.eval(Database.initialize(transactor))
      finalHttpApp = Logger.httpApp[IO](logHeaders = true, logBody = true)(buildRoutes(transactor))
      exitCode <- BlazeServerBuilder[IO](global)
        .bindHttp(config.server.port, config.server.host)
        .enableHttp2(true)
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
}

object HttpServer {
  def apply(implicit executionContext: ExecutionContext): HttpServer =
    new HttpServer()
}
