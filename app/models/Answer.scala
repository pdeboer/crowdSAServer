package models

import com.github.tototoshi.slick.H2JodaSupport._
import org.joda.time.DateTime
import play.api.libs.json._

import play.api.libs.functional.syntax._

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.Tag

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Option[Int],answer: String, answerTime: DateTime, turkerId:Int, qId: Int)


class Answers(tag:Tag) extends Table[Answer](tag, "answers") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def answer = column[String]("answer")
  def answerTime = column[DateTime]("answerTime", O.NotNull)
  def turkerId = column[Int]("turkerId", O.NotNull)
  def qId = column[Int]("qId", O.NotNull)

  def * = (id.?, answer, answerTime, turkerId, qId) <> (Answer.tupled, Answer.unapply)
}

object Answers {

  implicit val AnswerReads: Reads[Answer] = (
    (__ \ "id").read[Option[Int]] and
      (__ \ 'answer).read[String] and
      (__ \ 'answerTime).read[DateTime] and
      (__ \ 'turkerId).read[Int] and
      (__ \ 'qId).read[Int]
    )(Answer)

  implicit val AnswerWrites: Writes[Answer] = (
  (__ \ "id").write[Option[Int]] and
    (__ \ 'answer).write[String] and
    (__ \ 'answerTime).write[DateTime] and
    (__ \ 'turkerId).write[Int] and
    (__ \ 'qId).write[Int]
  )(unlift(Answer.unapply))

  lazy val answers = TableQuery[Answers]

  def add(answer: Answer, session: Session) =
    answers.insert(answer)(session)

  def list(session: Session): List[Answer] =
    answers.list(session)

  def getAnswersFromQuestionId(id: Int, session: Session): List[Answer] =
    answers.filter(_.qId === id).list(session)

}