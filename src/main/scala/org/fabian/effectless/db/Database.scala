package org.fabian.effectless.db

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import org.flywaydb.core.Flyway
import org.fabian.effectless.config.DatabaseConfig

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object Database {

  import cats.effect._
  import doobie.hikari._
  def transactor(
    dbConfig: DatabaseConfig
  )(implicit cs: ContextShift[IO]): IO[HikariTransactor[IO]] = {
    val config = new HikariConfig()
    config.setJdbcUrl(dbConfig.url)
    config.setUsername(dbConfig.user)
    config.setPassword(dbConfig.password)
    //config.setMaximumPoolSize(dbConfig.poolSize)

    for {
      es <- IO(Executors.newFixedThreadPool(32))
      ec = ExecutionContext.fromExecutor(es)
      be   <- IO(Blocker.liftExecutionContext(ec))
      resp <- IO.pure(HikariTransactor.apply[IO](new HikariDataSource(config), ec, be))
    } yield resp
  }

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] =
    transactor.configure { datasource =>
      IO {
        val flyWay = new Flyway()
        flyWay.setDataSource(datasource)
        flyWay.migrate()
      }
    }

}
