package models

import play.api.db.slick.Config.driver.simple.Session
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.Tag
/**
 * Created by Mattia on 24.12.2014.
 */

case class Question(id: Option[Int], question: String, answerType: String, hit_fk: Int)

class Questions(tag:Tag) extends Table[Question](tag, "questions") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def question = column[String]("question", O.NotNull)
  def answerType = column[String]("answerType", O.NotNull)
  def hit_fk = column[Int]("hit_fk", O.NotNull)

  def * = (id.?, question, answerType, hit_fk) <> (Question.tupled, Question.unapply)
}

  object Questions {

    implicit val QuestionReads: Reads[Question] = (
      (__ \ "id").read[Option[Int]] and
        (__ \ 'question).read[String] and
        (__ \ 'answerType).read[String] and
        (__ \ 'hit_fk).read[Int]
      )(Question)

    lazy val questions = TableQuery[Questions]

    def findByQuestionId(questionId: Int, session: Session): List[Question] =
      questions.filter(_.id === questionId).list(session)

    def findByHitId(hitId: Int, session: Session) : List[Question] =
      questions.filter(_.hit_fk === hitId).list(session)

    def add(question: Question, session: Session) =
      questions.insert(question)(session)

    def list(session: Session): List[Question] =
      questions.list(session)
  }