package org.fabian.effectless.domain.todo

import cats.Show
import io.circe.{ Decoder, Encoder }

import java.io.Serializable

abstract sealed class Importance(val value: String)

object Importance {
  case object High extends Importance("high")
  case object Medium extends Importance("medium")
  case object Low extends Importance("low")

  private def values = Set(High, Medium, Low)

  def unsafeFromString(value: String): Importance =
    values.find(_.value == value).get

  implicit val encodeImportance: Encoder[Importance] =
    Encoder.encodeString.contramap[Importance](_.value)

  implicit val decodeImportance: Decoder[Importance] =
    Decoder.decodeString.map[Importance](Importance.unsafeFromString)

}

case class Todo(id: Option[Long], description: String, importance: Importance)

object Todo {

  sealed trait TodoError extends Product with Serializable
  case class TodoNotFoundError(id: Option[Long]) extends TodoError

  implicit val todoErrorShow: Show[TodoError] = {
    case TodoNotFoundError(maybeLong) =>
      s"Todo with Id <${maybeLong.getOrElse("")}> was not found"
  }

}
