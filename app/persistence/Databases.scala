package persistence

import java.sql.Connection

import play.api.Application
import play.api.db._

/**
 * Wraps a named db-configuration in one object so that the name
 * does not need to be repeated throughout the codebase
 *
 * @param name The name of the connection, as set in conf/application.conf
 * @param app The db-connections are connected to the app lifecycle so therefore
 *            a db connection depens on having a running app
 */
class NamedDB(name: String)(implicit app: Application) {
  /** @see [[play.api.db.DB.withConnection]] */
  def withConnection[A](f: Connection => A): A = DB.withConnection[A](name)(f)(app)
  /** @see [[play.api.db.DB.withTransaction)]] */
  def withTransaction[A](f: Connection => A): A = DB.withTransaction[A](name)(f)(app)
}

object Databases {
  import play.api.Play.current
  object InMemory extends NamedDB("memory")
  object Mysql extends NamedDB("mysql")
}