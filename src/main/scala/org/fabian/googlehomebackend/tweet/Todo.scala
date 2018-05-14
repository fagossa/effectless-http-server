package org.fabian.googlehomebackend.tweet

import io.circe.{Decoder, Encoder}

abstract sealed class Importance(val value: String)

object Importance {
  case object High extends Importance("high")
  case object Medium extends Importance("medium")
  case object Low extends Importance("low")

  private def values = Set(High, Medium, Low)

  def unsafeFromString(value: String): Importance = {
    values.find(_.value == value).get
  }

  implicit val encodeImportance: Encoder[Importance] = Encoder.encodeString.contramap[Importance](_.value)

  implicit val decodeImportance: Decoder[Importance] = Decoder.decodeString.map[Importance](Importance.unsafeFromString)

}

case class Todo(id: Option[Long], description: String, importance: Importance)

case object TodoNotFoundError