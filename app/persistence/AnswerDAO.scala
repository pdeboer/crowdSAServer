package persistence

import java.util.Date
import anorm._
import anorm.SqlParser._
import models.{Assignment, Answer, Paper}
import play.api.db.DB
import play.api.Play.current

import scala.collection.mutable

/**
 * Created by Mattia on 22.01.2015.
 */
object AnswerDAO {

  private val answerParser: RowParser[Answer] =
    get[Pk[Long]]("id") ~
      get[String]("answer") ~
      get[Long]("completedTime") ~
      get[Option[Boolean]]("accepted") ~
      get[Option[Boolean]]("acceptedAndBonus") ~
      get[Option[Boolean]]("rejected") ~
      get[Long]("assignment_fk") map {
      case id ~answer ~completedTime ~accepted ~acceptedAndBonus ~rejected ~assignment_fk => Answer(id, answer, completedTime, accepted, acceptedAndBonus, rejected, assignment_fk)
    }

  def findById(id: Long): Option[Answer] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM answers WHERE id = {id}").on(
        'id -> id
      ).as(answerParser.singleOpt)
    }

  def evaluateAnswer(aId: Long, accepted: Boolean, bonus: Boolean): Int = {
    if (accepted && bonus) {
      DB.withConnection { implicit c =>
        SQL("UPDATE answers SET accepted = true, acceptedAndBonus = true, rejected = false WHERE id = {aId}")
          .on('aId -> aId).executeUpdate()
      }
    } else if (accepted && !bonus) {
      DB.withConnection { implicit c =>
        SQL("UPDATE answers SET accepted = true, acceptedAndBonus = false, rejected = false WHERE id = {aId}")
          .on('aId -> aId).executeUpdate()
      }
    } else {
      DB.withConnection { implicit c =>
        SQL("UPDATE answers SET accepted = false, acceptedAndBonus = false, rejected = true WHERE id = {aId}")
          .on('aId -> aId).executeUpdate()
      }
    }
  }

  def add(a: Answer): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO answers(answer, completedTime, assignment_fk) VALUES ({answer}, {completedTime}, {assignment_fk})").on(
          'answer -> a.answer,
          'completedTime -> a.completedTime,
          'assignment_fk -> a.assignment_fk
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Answer] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM answers").as(answerParser*).toList
    }

  def getAllByAssignmentsIds(assignments: List[Assignment]) : List[Answer] = {
    var answers = new mutable.MutableList[Answer]
    for (assignment <- assignments) {
      DB.withConnection { implicit c =>
        val tmpAnswers = SQL("SELECT * FROM answers WHERE assignment_fk = {assignmentId}")
          .on('assignmentId -> assignment.id.get)
          .as(answerParser*).toList

        for (ans <- tmpAnswers) {
          answers += ans
        }
      }
    }
    answers.toList
  }

}
