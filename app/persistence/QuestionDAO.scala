package persistence

import java.util.Date

import anorm._
import anorm.SqlParser._
import models.{Question, Paper}
import play.api.db.DB
import play.api.Play.current

/**
 * Created by Mattia on 12.01.2015.
 */

object QuestionDAO {

  private val questionParser: RowParser[Question] =
    get[Pk[Long]]("id") ~
      get[String]("question") ~
      get[String]("questionType") ~
      get[Int]("reward") ~
      get[Long]("createdAt") ~
      get[Boolean]("disabled") ~
      get[Long]("paper_fk") map {
      case id ~question ~questionType ~reward ~createdAt ~disabled ~paper_fk => Question(id, question, questionType, reward, createdAt, disabled, paper_fk)
    }

  def findById(id: Long): Option[Question] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE id = {id} AND disabled=false").on(
      'id -> id
      ).as(questionParser.singleOpt)
    }

  def getRandomQuestionRandomPaper(turkerId: String): Long = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      val randomQuestion = SQL("SELECT * FROM questions AS q WHERE disabled=false AND NOT EXISTS (SELECT * FROM assignments WHERE ( (assignedTo)/1000 > UNIX_TIMESTAMP() or team_fk = {teamId}) AND question_fk = q.id) GROUP BY RAND() LIMIT 1")
        .on('teamId -> teamId)
        .as(questionParser.singleOpt)
      randomQuestion.get.id.get
    }
  }

  def add(q: Question): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO questions(question, questionType, reward, createdAt, disabled, paper_fk) VALUES (" +
          "{question}, " +
          "{questionType}, " +
          "{reward}, " +
          "{createdAt}, " +
          "{disabled}, " +
          "{paper_fk})").on(
              'question -> q.question,
              'questionType -> q.questionType,
              'reward -> q.reward,
              'createdAt -> q.createdAt,
              'disabled -> q.disabled,
              'paper_fk -> q.paper_fk
          ).executeInsert()
      }
    id.get
  }

  def disable(id: Long) =
    DB.withConnection { implicit c =>
      SQL("UPDATE questions SET disabled=true WHERE id = {id}").on(
        'id -> id
      ).executeUpdate()
    }


  def getAllEnabled(turkerId: String): List[Question] = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AS q WHERE disabled=false AND NOT EXISTS (SELECT * FROM assignments WHERE ( (assignedTo)/1000 > UNIX_TIMESTAMP() or team_fk = {teamId}) AND question_fk = q.id)")
        .on('teamId -> teamId.toString)
        .as(questionParser *)
    }
  }

  def getRandomQuestionSamePaper(turkerId: String, paperId: Long): Long = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AS q WHERE paper_fk = {paperId} AND disabled=false AND NOT EXISTS (SELECT * FROM assignments WHERE ( (assignedTo)/1000 > UNIX_TIMESTAMP() or team_fk = {teamId}) AND question_fk = q.id) GROUP BY RAND() LIMIT 1")
        .on('paperId -> paperId,
        'teamId -> teamId)
        .as(questionParser.single).id.get
    }
  }

  def getSameQuestionTypeRandomPaper(turkerId: String, questionType: String): Long = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AS q WHERE questionType = {questionType} AND NOT EXISTS (SELECT * FROM assignments WHERE ( (assignedTo)/1000 > UNIX_TIMESTAMP() or team_fk = {teamId}) AND question_fk = q.id) AND disabled=false GROUP BY RAND() LIMIT 1")
        .on('questionType -> questionType,
        'teamId -> teamId)
        .as(questionParser.single).id.get
    }
  }

  def getRandomQuestionType(turkerId: String): String = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AS q WHERE disabled=false AND NOT EXISTS (SELECT * FROM assignments WHERE team_fk = {teamId} AND question_fk = q.id) GROUP BY RAND() LIMIT 1")
        .on('teamId -> teamId)
        .as(questionParser.single).questionType
    }
  }

}