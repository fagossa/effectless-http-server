package org.fabian.effectless.http

import cats.effect.IO
import org.http4s.dsl.Http4sDsl

trait HttpOps {

  self: Http4sDsl[IO] =>

  import io.circe.syntax._
  import org.http4s._
  import org.http4s.circe._

  def resultOrNotFound[A, B](
    result: Either[A, B]
  )(implicit show: cats.Show[A], encoder: io.circe.Encoder[B]): IO[Response[IO]] =
    result match {
      case Left(error) => NotFound(show.show(error))
      case Right(todo) => Ok(todo.asJson)
    }
}
