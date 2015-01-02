package models

import com.github.tototoshi.slick.H2JodaSupport._
import org.joda.time.DateTime

import play.api.libs.json._

import play.api.libs.functional.syntax._

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.Tag

/**
 * Created by Mattia on 26.12.2014.
 */

case class Hit(id: Option[Int], pdf: String, hitReceived: DateTime)

class Hits(tag:Tag) extends Table[Hit](tag, "hits") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def pdf = column[String]("pdf", O.NotNull)
  def hitReceived = column[DateTime]("hitReceived", O.NotNull)

  def * = (id.?, pdf, hitReceived) <> (Hit.tupled, Hit.unapply)
}

object Hits {

  implicit val HitReads: Reads[Hit] = (
    (__ \ "id").read[Option[Int]] and
      (__ \ 'pdf).read[String] and
      (__ \ 'hitReceived).read[DateTime]// and
      //(__ \ 'questions).read(List[Question])
  )(Hit)

  lazy val hits = TableQuery[Hits]

  def add(hit: Hit, session: Session) =
    hits.insert(hit)(session)

  def list(session: Session): List[Hit] =
    hits.list(session)

  def findHitById(id: Int, session: Session): List[Hit] =
    hits.filter(_.id === id).list(session)

}