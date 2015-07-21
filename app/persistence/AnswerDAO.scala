package persistence

import anorm.SqlParser._
import anorm._
import models.{Answer, Assignment}
import play.api.Play.current
import play.api.db.DB

import scala.collection.mutable

/**
 * Created by Mattia on 22.01.2015.
 */
object AnswerDAO {

  private val answerParser: RowParser[Answer] =
    get[Pk[Long]]("id") ~
      get[String]("answer") ~
      get[Option[String]]("motivation") ~
      get[Option[String]]("observation") ~
      get[Long]("created_at") ~
      get[Boolean]("is_method_used") ~
      get[Option[Boolean]]("accepted") ~
      get[Option[Int]]("bonus_cts") ~
      get[Option[Boolean]]("rejected") ~
      get[Long]("assignments_id") ~
      get[String]("accuracy") map {
      case id ~answer ~motivation ~observation ~completed_at ~is_method_used ~accepted ~bonus_cts ~rejected ~assignments_id ~accuracy =>
        Answer(id, answer, motivation, observation, completed_at, is_method_used, accepted, bonus_cts, rejected, assignments_id, accuracy)
    }

  def findById(id: Long): Option[Answer] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM answers WHERE id = {id}").on(
        'id -> id
      ).as(answerParser.singleOpt)
    }

  def evaluateAnswer(id: Long, accepted: Boolean, bonus_cts: Option[Int]): Int = {
   if (accepted) {
      DB.withConnection { implicit c =>
        SQL("UPDATE answers SET accepted = true, bonus_cts = {bonus_cts}, rejected = false WHERE id = {id}")
          .on(
            'id -> id,
            'bonus_cts -> bonus_cts.getOrElse(0)).executeUpdate()
      }
    } else {
      DB.withConnection { implicit c =>
        SQL("UPDATE answers SET accepted = false, bonus_cts = 0, rejected = true WHERE id = {id}")
          .on('id -> id).executeUpdate()
      }
    }
  }

  def add(a: Answer): Long = {
    try {
      val id: Option[Long] =
        DB.withConnection { implicit c =>
          SQL("INSERT INTO answers(answer, motivation, observation, created_at, is_method_used, accepted, bonus_cts, rejected," +
            " assignments_id, accuracy) VALUES ({answer}, {motivation}, {observation}, {created_at}, {is_method_used}, NULL, NULL, NULL, " +
            "{assignments_id}, {accuracy})").on(
            'answer -> a.answer,
            'motivation -> a.motivation,
            'observation -> a.observation,
            'created_at -> a.createdAt,
            'is_method_used -> a.isMethodUsed,
            'assignments_id -> a.assignmentsId,
            'accuracy -> a.accuracy
          ).executeInsert()
        }
      id.get
    }catch{
      case e: Exception => {
        e.printStackTrace()
        return -1
      }
    }
  }

  def getAll(): List[Answer] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM answers").as(answerParser*).toList
    }

  def getAllByAssignmentsIds(assignments: List[Assignment]) : List[Answer] = {
    var answers = new mutable.MutableList[Answer]
    for (assignment <- assignments) {
      DB.withConnection { implicit c =>
        val tmpAnswers = SQL("SELECT * FROM answers WHERE assignments_id = {assignments_id}")
          .on('assignments_id -> assignment.id.get)
          .as(answerParser*).toList

        for (ans <- tmpAnswers) {
          answers += ans
        }
      }
    }
    answers.toList
  }

  def getAcceptedAnswers(turker_id: String): Int ={
    DB.withConnection { implicit c =>
      val teamsId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turker_id).id.get.toString
      val count = SQL("SELECT COUNT(*) AS count FROM answers AS ans WHERE accepted = true AND " +
        "EXISTS ( SELECT assignments_id FROM assignments WHERE id = ans.assignments_id AND teams_id = {teamsId})")
        .on('teamsId -> teamsId)
        .apply().head
      try {
        val res = count[Long]("count")
        res.toInt
      } catch {
        case e: Exception => return 0
      }
    }
  }

  def getByAssignmentId(assignment_id: Long): Option[Answer] = {
    try {
      DB.withConnection { implicit c =>
        SQL("SELECT * FROM answers WHERE assignments_id = {assignments_id}")
          .on('assignments_id -> assignment_id)
          .as(answerParser.singleOpt)
      }
    }catch {
      case e: Exception => {
        e.printStackTrace()
        None
      }
    }
  }

}
