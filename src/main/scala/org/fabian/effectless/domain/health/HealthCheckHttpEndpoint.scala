package org.fabian.effectless.domain.health

import cats.Show.ToShowOps
import cats.effect.IO
import org.fabian.effectless.http.HttpOps
import org.http4s.{ HttpRoutes, HttpService, MediaType }
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

class HealthCheckHttpEndpoint(root: String) extends Http4sDsl[IO] with HttpOps with ToShowOps {

  val service: HttpRoutes[IO] = HttpService[IO] { // HttpRoutes
    case GET -> Root / root =>
      Ok(
        "Ok",
        `Content-Type`(MediaType.application.json)
      )
  }

}
