package org.fabian.effectless.tweet

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import doobie._
import doobie.implicits._
import org.fabian.effectless.tweet.Todo.TodoError
import org.fabian.effectless.tweet.Todo.TodoNotFoundError

class TodoRepository(transactor: Transactor[IO]) {
  private implicit val importanceMeta: Meta[Importance] =
    Meta[String].xmap(Importance.unsafeFromString, _.value)

  import cats.syntax.option._

  def getTodos: Stream[IO, Todo] =
    sql"SELECT id, description, importance FROM todo".query[Todo].stream.transact(transactor)

  def getTodo(id: Long): IO[Either[TodoError, Todo]] =
    sql"SELECT id, description, importance FROM todo WHERE id = $id"
      .query[Todo]
      .option
      .transact(transactor)
      .map {
        case Some(todo) => Right(todo)
        case None       => Left(TodoNotFoundError(id.some))
      }

  def createTodo(todo: Todo): IO[Todo] =
    sql"INSERT INTO todo (description, importance) VALUES (${todo.description}, ${todo.importance})".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id =>
        todo.copy(id = id.some)
      }

  def deleteTodo(id: Long): IO[Either[TodoError, Unit]] =
    sql"DELETE FROM todo WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(TodoNotFoundError(id.some))
      }
    }

  def updateTodo(id: Long, todo: Todo): IO[Either[TodoError, Todo]] =
    sql"UPDATE todo SET description = ${todo.description}, importance = ${todo.importance} WHERE id = $id".update.run
      .transact(transactor)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(todo.copy(id = id.some))
        } else {
          Left(TodoNotFoundError(id.some))
        }
      }
}
