package persistence

import anorm.SqlParser._
import anorm._
import models.Assignment
import play.api.Logger
import play.api.Play.current
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
      ).as(assignmentParser*)
    }


  def findByAnswerId(ansId: Long): Option[Assignment] = {
    DB.withConnection { implicit c =>
      val answer = AnswerDAO.findById(ansId).get
      SQL("SELECT * FROM assignments WHERE id = {assignments_id}").on(
        'assignments_id -> answer.assignments_id
      ).as(assignmentParser.singleOpt)
    }
  }

  def isAnAssignmentAlreadyOpen(turker_id: String): Boolean = {
    DB.withConnection { implicit c =>
      val teams_id = Turkers2TeamsDAO.findSingleTeamByTurkerId(turker_id).id.get

      SQL("SELECT * FROM questions AS q WHERE " +
        "q.disabled=false AND EXISTS (SELECT * FROM assignments AS a WHERE " +
        "(a.expiration_time < UNIX_TIMESTAMP() OR a.teams_id = {teams_id}) AND " +
        "a.is_cancelled = false AND (SELECT COUNT(*) FROM answers WHERE assignments_id = a.id) = 0 AND " +
        "a.questions_id = q.id)").on(
          'teams_id -> teams_id
        ).as(QuestionDAO.questionParser*).length > 0
    }
  }

  def getOpenAssignment(turker_id: String): Assignment = {
    DB.withConnection { implicit c =>
      val teams_id = Turkers2TeamsDAO.findSingleTeamByTurkerId(turker_id).id.get

      val question = SQL("SELECT * FROM questions AS q WHERE " +
        "q.disabled=false AND EXISTS (SELECT * FROM assignments AS a WHERE " +
        "(a.expiration_time < UNIX_TIMESTAMP() OR a.teams_id = {teams_id}) AND " +
        "a.is_cancelled = false AND (SELECT COUNT(*) FROM answers WHERE assignments_id = a.id) = 0 AND " +
        "a.questions_id = q.id)").on(
          'teams_id -> teams_id
        ).as(QuestionDAO.questionParser.singleOpt)
      if(question.isDefined) {
        val assignments = findByQuestionId(question.get.id.get)
        Logger.debug("Found " + assignments.length + " assignments")
        var result: Assignment = null
        if(assignments.length == 1) {
          result = assignments.head
        }
        assignments.foreach(a => {
          Logger.debug("Assignment: " + a)
          if(a.is_cancelled==false && a.teams_id==teams_id
            && !AnswerDAO.getByAssignmentId(a.id.get).isDefined){
            //&& a.expiration_time < (new Date().getTime()/1000)
            Logger.debug("Found open assignment")
            result = a
          }
        })
        result
      } else {
        Logger.debug("Cannot find assignments for this question")
        null
      }
    }
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
      SQL("SELECT * FROM assignments").as(assignmentParser*)
    }

  def cancel(assignment_id: Long) = {
    DB.withConnection { implicit c =>
      SQL("UPDATE assignments SET is_cancelled = true WHERE id = {assignment_id}")
        .on('assignment_id -> assignment_id).executeUpdate()
    }
  }
}
