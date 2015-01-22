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
      get[Long]("assignedFrom") ~
      get[Long]("assignedTo") ~
      get[Long]("acceptedTime") ~
      get[Long]("question_fk") ~
      get[Long]("team_fk") map {
      case id ~assignedFrom ~assignedTo ~acceptedTime ~question_fk ~team_fk => Assignment(id, assignedFrom, assignedTo, acceptedTime, question_fk, team_fk)
    }

  def findById(id: Long): Option[Assignment] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM assignments WHERE id = {id}").on(
        'id -> id
      ).as(assignmentParser.singleOpt)
    }

  def add(a: Assignment, q: Long, t: Long): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO assignments(assignedFrom, assignedTo, acceptedTime, question_fk, team_fk) VALUES ({assignedFrom}, {assignedTo}, {acceptedTime}, {question_fk}, {team_fk})").on(
          'assignedFrom -> a.assignedFrom,
          'assignedTo -> a.assignedTo,
          'acceptedTime -> a.acceptedTime,
          'question_fk -> q,
          'team_fk -> t
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Assignment] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM assignments").as(assignmentParser*).toList
    }
}
