package models

import com.github.tototoshi.slick.H2JodaSupport._
import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple.Session
import scala.slick.lifted.Tag
import scala.slick.driver.H2Driver.simple._

/**
 * Created by Mattia on 22.12.2014.
 */

case class Turker(id: Option[Int], turkerId: String, loginTime: DateTime)

class Turkers(tag:Tag) extends Table[Turker](tag, "turkers") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def turkerId = column[String]("turkerId", O.NotNull)
  def loginTime = column[DateTime]("loginTime", O.NotNull)

  def * = (id.?, turkerId, loginTime) <> (Turker.tupled, Turker.unapply)
}

  object Turkers {

    lazy val turkers = TableQuery[Turkers]

    def findByTurkerId(turkerId: String, session: Session): List[Turker] =
      turkers.filter(_.turkerId === turkerId).list(session)

    def add(turker: Turker, session: Session) =
      turkers.insert(turker)(session)

    def list(session: Session): List[Turker] =
      turkers.list(session)

  }