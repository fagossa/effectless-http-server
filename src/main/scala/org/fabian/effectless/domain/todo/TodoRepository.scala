package org.fabian.effectless.domain.todo

import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import fs2.Stream
import org.fabian.effectless.domain.todo.Todo.TodoError
import org.fabian.effectless.domain.todo.Todo.TodoNotFoundError

class TodoRepository(xa: Transactor[IO]) {
  import cats.syntax.option._

  def getTodos: Stream[IO, Todo] =
    TodoStatement.findTodos.stream.transact(xa)

  def getTodo(id: Long): IO[Either[TodoError, Todo]] =
    TodoStatement
      .findTodoById(id)
      .transact(xa)
      .map {
        case Some(todo) => Right(todo)
        case None       => Left(TodoNotFoundError(id.some))
      }

  def createTodo(todo: Todo): IO[Todo] =
    TodoStatement
      .createTodoQuery(todo)
      .transact(xa)
      .map { id =>
        todo.copy(id = id.some)
      }

  def deleteTodo(id: Long): IO[Either[TodoError, Unit]] =
    TodoStatement
      .deleteTodoQuery(id)
      .transact(xa)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(())
        } else {
          Left(TodoNotFoundError(id.some))
        }
      }

  def updateTodo(id: Long, todo: Todo): IO[Either[TodoError, Todo]] =
    TodoStatement
      .updateTodoQuery(id, todo)
      .transact(xa)
      .map { affectedRows =>
        if (affectedRows == 1) {
          Right(todo.copy(id = id.some))
        } else {
          Left(TodoNotFoundError(id.some))
        }
      }
}

object TodoStatement {

  Importance.High.value

  import doobie.util.meta.Meta
  private implicit val importanceMeta: Meta[Importance] =
    Meta[String].imap(Importance.unsafeFromString)(_.value)

  def findTodos: Query0[Todo] =
    sql"SELECT id, description, importance FROM todo"
      .query[Todo]

  def findTodoById(id: Long): doobie.ConnectionIO[Option[Todo]] =
    sql"SELECT id, description, importance FROM todo WHERE id = $id"
      .query[Todo]
      .option

  def createTodoQuery(todo: Todo): ConnectionIO[Long] =
    sql"INSERT INTO todo (description, importance) VALUES (${todo.description}, ${todo.importance})".update
      .withUniqueGeneratedKeys[Long]("id")

  def deleteTodoQuery(id: Long): ConnectionIO[Int] =
    sql"DELETE FROM todo WHERE id = $id".update.run

  def updateTodoQuery(id: Long, todo: Todo): ConnectionIO[Int] =
    sql"UPDATE todo SET description = ${todo.description}, importance = ${todo.importance} WHERE id = $id".update.run
}
