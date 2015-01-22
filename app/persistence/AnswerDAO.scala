package persistence

import java.util.Date
import anorm._
import anorm.SqlParser._
import models.{Answer, Paper}
import play.api.db.DB
import play.api.Play.current
/**
 * Created by Mattia on 22.01.2015.
 */
object AnswerDAO {
  private val answerParser: RowParser[Answer] =
    get[Pk[Long]]("id") ~
      get[String]("answer") ~
      get[Long]("completedTime") ~
      get[Boolean]("accepted") ~
      get[Boolean]("acceptedAndBonus") ~
      get[Boolean]("rejected") ~
      get[Long]("question_fk") ~
      get[Long]("team_fk") map {
      case id ~answer ~completedTime ~accepted ~acceptedAndBonus ~rejected ~question_fk ~team_fk => Answer(id, answer, completedTime, accepted, acceptedAndBonus, rejected, question_fk, team_fk)
    }

  def findById(id: Long): Option[Answer] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM answers WHERE id = {id}").on(
        'id -> id
      ).as(answerParser.singleOpt)
    }

  def add(a: Answer, q: Long, t: Long): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO answers(answer, completedTime, accepted, acceptedAndBonus, rejected, question_fk, team_fk) VALUES ({answer}, {completedTime}, {accepted}, {acceptedAndBonus}, {rejected}, {question_fk}, {team_fk})").on(
          'answer -> a.answer,
          'completedTime -> (new Date()).getTime,
          'accepted -> null,
          'acceptedAndBonus -> null,
          'rejected -> null,
          'question_fk -> q,
          'team_fk -> t
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Answer] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM answers").as(answerParser*).toList
    }
}
