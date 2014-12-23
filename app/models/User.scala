package models

import com.github.tototoshi.slick.H2JodaSupport._
import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple.Session

import scala.slick.driver.H2Driver.simple._

/**
 * Created by Mattia on 22.12.2014.
 */

case class User(id: Option[Int], turkerId: String, loginTime: DateTime)

class Users(tag:Tag) extends Table[User](tag, "users") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def turkerId = column[String]("turkerId", O.NotNull)
  def loginTime = column[DateTime]("loginTime", O.NotNull)

  def * = (id.?, turkerId, loginTime) <> (User.tupled, User.unapply)
}

  object Users {

    lazy val users = TableQuery[Users]

    def findByTurkerId(turkerId: String, session: Session): List[User] =
      users.filter(_.turkerId === turkerId).list(session)

    def add(user: User, session: Session) =
      users.insert(user)(session)

    def list(session: Session): List[User] =
      users.list(session)

  }