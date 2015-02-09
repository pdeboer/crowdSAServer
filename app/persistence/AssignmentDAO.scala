package persistence

import java.util.Date
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import models.{Assignment, Answer}
import play.api.db.DB

/**
 * Created by Mattia on 22.01.2015.
 */
object AssignmentDAO {

  private val assignmentParser: RowParser[Assignment] =
    get[Pk[Long]]("id") ~
      get[Long]("created_at") ~
      get[Long]("expiration_time") ~
      get[Boolean]("is_cancelled") ~
      get[Long]("questions_id") ~
      get[Long]("teams_id") map {
      case id ~created_at ~expiration_time ~is_cancelled ~questions_id ~teams_id => Assignment(id, created_at, expiration_time, is_cancelled, questions_id, teams_id)
    }

  def findById(id: Long): Option[Assignment] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM assignments WHERE id = {id}").on(
        'id -> id
      ).as(assignmentParser.singleOpt)
    }

  def findByQuestionId(qId: Long): List[Assignment] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM assignments WHERE questions_id = {qId}").on(
        'qId -> qId
      ).as(assignmentParser*).toList
    }

  def add(a: Assignment): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO assignments(created_at, expiration_time, is_cancelled, questions_id, teams_id) VALUES ({created_at}, {expiration_time}, {is_cancelled}, {questions_id}, {teams_id})").on(
          'created_at -> a.created_at,
          'expiration_time-> a.expiration_time,
          'is_cancelled -> a.is_cancelled,
          'questions_id -> a.questions_id,
          'teams_id -> a.teams_id
        ).executeInsert()
      }
    id.get
  }

  def remove(assignmendId: Long) = {
    DB.withConnection { implicit c =>
      SQL("DELETE FROM assignments WHERE id = {id}").on(
        'id -> assignmendId
      ).execute()
    }
  }

  def getAll(): List[Assignment] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM assignments").as(assignmentParser*).toList
    }
}
