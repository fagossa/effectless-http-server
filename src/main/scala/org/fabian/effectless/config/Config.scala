package org.fabian.effectless.config

import cats.effect.IO
import com.typesafe.config.ConfigFactory

case class ServerConfig(host: String, port: Int)

case class DatabaseConfig(driver: String, url: String, user: String, password: String)

case class AppConfig(server: ServerConfig, database: DatabaseConfig)

object AppConfig {
  import pureconfig.generic.auto._
  import pureconfig.ConfigSource
  import pureconfig.error.ConfigReaderException

  def load(configFile: String = "application.conf"): IO[AppConfig] =
    IO {
      ConfigSource.fromConfig(ConfigFactory.load(configFile)).load[AppConfig]
    }.flatMap {
      case Right(config) => IO.pure(config)
      case Left(e)       => IO.raiseError[AppConfig](new ConfigReaderException[AppConfig](e))
    }
}
