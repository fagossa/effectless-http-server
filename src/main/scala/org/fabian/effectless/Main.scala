package org.fabian.effectless

import cats.effect.{ ExitCode, IO, IOApp }
import config.Config
/*import fs2.StreamApp
import fs2.StreamApp.ExitCode*/

import org.http4s.dsl.Http4sDsl

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp with Http4sDsl[IO] {

  def run(args: List[String]): IO[ExitCode] =
    HttpServer.stream().compile.drain.as(ExitCode.Success)

}
