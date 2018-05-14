package org.fabian.effectless.tweet

import cats.Show.ToShowOps
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import fs2.Stream
import org.http4s.{HttpService, MediaType, Response, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import org.http4s.headers.{Location, `Content-Type`}
import org.fabian.effectless.tweet.Todo.TodoError

class TodoService(repository: TodoRepository) extends Http4sDsl[IO] with ToShowOps {

  val service = HttpService[IO] {
    case GET -> Root / "todos" =>
      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.`application/json`))

    case GET -> Root / "todos" / LongVar(id) =>
      for {
        getResult <- repository.getTodo(id)
        response <- toResult(getResult)
      } yield response

    case req @ POST -> Root / "todos" =>
      for {
        todo <- req.decodeJson[Todo]
        createdTodo <- repository.createTodo(todo)
        response <- Created(createdTodo.asJson, Location(Uri.unsafeFromString(s"/todos/${createdTodo.id.get}")))
      } yield response

    case req @ PUT -> Root / "todos" / LongVar(id) =>
      for {
        todo <-req.decodeJson[Todo]
        updateResult <- repository.updateTodo(id, todo)
        response <- toResult(updateResult)
      } yield response

    case DELETE -> Root / "todos" / LongVar(id) =>
      repository.deleteTodo(id).flatMap {
        case Left(error) => NotFound(error.show)
        case Right(_) => NoContent()
      }
  }

  private def toResult(result: Either[TodoError, Todo]): IO[Response[IO]] = {
    result match {
      case Left(error) => NotFound(error.show)
      case Right(todo) => Ok(todo.asJson)
    }
  }
}