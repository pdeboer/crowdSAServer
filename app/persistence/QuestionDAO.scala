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
      get[String]("question_type") ~
      get[Int]("reward_cts") ~
      get[Long]("created_at") ~
      get[Boolean]("disabled") ~
      get[Option[Long]]("expiration_time_sec") ~
      get[Option[Int]]("maximal_assignments") ~
      get[Long]("papers_id") map {
      case id ~question ~question_type ~reward_cts ~created_at ~disabled ~expiration_time_sec ~maximal_assignments ~papers_id
      => Question(id, question, question_type, reward_cts, created_at, disabled, expiration_time_sec, maximal_assignments, papers_id)
    }

  def findById(id: Long): Option[Question] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE id = {id}").on(
      'id -> id
      ).as(questionParser.singleOpt)
    }

  def getRandomQuestionRandomPaper(turkerId: String): Long = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      val randomQuestion = SQL("SELECT * FROM questions AS q WHERE disabled=false AND (expiration_time_sec > UNIX_TIMESTAMP() OR expiration_time_sec = NULL) NOT EXISTS (SELECT * FROM assignments WHERE ( expiration_time_sec > UNIX_TIMESTAMP() or teams_id = {teamId}) AND questions_id = q.id) AND NOT (SELECT * FROM assignments WHERE questions_id = q.id) > maximal_assignments GROUP BY RAND() LIMIT 1")
        .on('teamId -> teamId)
        .as(questionParser.singleOpt)
      randomQuestion.get.id.get
    }
  }

  def add(q: Question): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO questions(question, question_type" +
          ", reward_cts, created_at, disabled, expiration_time_sec, maximal_assignments, papers_id) VALUES (" +
          "{question}, " +
          "{question_type}, " +
          "{reward_Cts}, " +
          "{created_at}, " +
          "{disabled}, " +
          "{expiration_time_sec}, " +
          "{maximal_assignments}, " +
          "{papers_id})").on(
              'question -> q.question,
              'question_type
                -> q.question_type,
              'reward_cts -> q.reward_cts,
              'created_at -> q.created_at,
              'disabled -> q.disabled,
              'expiration_time_sec -> q.expiration_time_sec,
              'maximal_assignments -> q.maximal_assignments,
              'papers_id -> q.papers_id
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
      SQL("SELECT * FROM questions AS q WHERE disabled=false AND " +
        "NOT EXISTS (SELECT * FROM assignments WHERE ( expiration_time_sec > UNIX_TIMESTAMP() OR teams_id = {teamId}) AND questions_id = q.id)" +
        "AND (SELECT COUNT(*) FROM assignments WHERE questions_id = q.id) < maximal_assignments")
        .on('teamId -> teamId.toString)
        .as(questionParser *)
    }
  }

  def getRandomQuestionSamePaper(turkerId: String, paperId: Long): Long = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AS q WHERE papers_id = {paperId} AND disabled=false AND " +
        "NOT EXISTS (SELECT * FROM assignments WHERE ( expiration_time_sec > UNIX_TIMESTAMP() OR teams_id = {teamId}) AND questions_id = q.id) " +
        "AND (SELECT COUNT(*) FROM assignments WHERE questions_id = q.id) < maximal_assignments GROUP BY RAND() LIMIT 1")
        .on('paperId -> paperId,
        'teamId -> teamId)
        .as(questionParser.single).id.get
    }
  }

  def getSameQuestionTypeRandomPaper(turkerId: String, question_type
  : String): Long = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AS q WHERE question_type" +
        " = {question_type" +
        "} AND NOT EXISTS (SELECT * FROM assignments WHERE ( expiration_time_sec > UNIX_TIMESTAMP() or teams_id = {teamId}) AND questions_id = q.id) AND disabled=false AND " +
        "(SELECT COUNT(*) FROM assignments WHERE questions_id = q.id) < maximal_assignments GROUP BY RAND() LIMIT 1")
        .on('question_type
        -> question_type
          ,
        'teamId -> teamId)
        .as(questionParser.single).id.get
    }
  }

  def getRandomQuestionType(turkerId: String): String = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AS q WHERE disabled=false AND NOT EXISTS (SELECT * FROM assignments WHERE teams_id = {teamId} AND questions_id = q.id) AND " +
        "(SELECT COUNT(*) FROM assignments WHERE questions_id = q.id) < maximal_assignments GROUP BY RAND() LIMIT 1")
        .on('teamId -> teamId)
        .as(questionParser.single).question_type

    }
  }

  def getTotalQuestionAnswered(turkerId: String): Int = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      val count =
        SQL("SELECT COUNT(*) FROM questions AS q WHERE EXISTS (SELECT * FROM assignments WHERE teams_id = {teamId} AND questions_id = q.id)")
          .on('teamId -> teamId)
          .apply().head
      try {
        val res = count[Long]("COUNT(*)")
        res.toInt
      } catch {
        case e: Exception => return 0
      }
    }
  }

  def getTotalAccepted(turkerId: String) : Int = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      val count =
        SQL("SELECT COUNT(*) FROM questions AS q WHERE EXISTS (SELECT * FROM assignments AS a WHERE teams_id = {teamId} AND questions_id = q.id AND EXISTS( SELECT * FROM answers WHERE accepted=true AND rejected=FALSE AND a.id = assignments_id ) )")
          .on('teamId -> teamId)
          .apply().head
      try {
        val res = count[Long]("COUNT(*)")
        res.toInt
      } catch {
        case e: Exception => return 0
      }
    }
  }

  def getTotalRejected(turkerId: String): Int = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      val count =
        SQL("SELECT COUNT(*) FROM questions AS q WHERE EXISTS (SELECT * FROM assignments AS a WHERE teams_id = {teamId} AND questions_id = q.id AND EXISTS( SELECT * FROM answers WHERE rejected=true AND accepted=false AND a.id = assignments_id ) )")
          .on('teamId -> teamId)
          .apply().head
      try {
        val res = count[Long]("COUNT(*)")
        res.toInt
      } catch {
        case e: Exception => return 0
      }
    }
  }

  def getTotalPending(turkerId: String) : Int = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      val count =
        SQL("SELECT COUNT(*) FROM questions AS q WHERE EXISTS (SELECT * FROM assignments AS a WHERE teams_id = {teamId} AND questions_id = q.id AND EXISTS( SELECT * FROM answers WHERE rejected IS NULL AND accepted IS NULL and bonus_cts IS NULL AND a.id = assignments_id ) )")
          .on('teamId -> teamId)
          .apply().head
      try {
        val res = count[Long]("COUNT(*)")
        res.toInt
      } catch {
        case e: Exception => return 0
      }
    }
  }

  def getQuestionsByPaperId(turker_id: String, paper_id: Long) = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turker_id).id.get.toString
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AS q WHERE papers_id = {paper_id} AND disabled=false AND NOT EXISTS (SELECT * FROM assignments WHERE ( expiration_time_sec > UNIX_TIMESTAMP() or teams_id = {teamId}) AND questions_id = q.id) AND IF(maximal_assignments IS NULL, TRUE, (SELECT COUNT(*) FROM assignments WHERE questions_id = q.id) < maximal_assignments)").on('paper_id -> paper_id, 'teamId -> teamId).as(questionParser*)
    }
  }

  def extendMaxAssignments(question_id: Long, maximal_assignments: Int) = {
    DB.withConnection{ implicit c =>
      SQL("UPDATE questions SET maximal_assignments = maximal_assignments + {maximal_assignments} WHERE id = {id}")
        .on('id -> question_id,
        'maximal_assignments -> maximal_assignments).executeUpdate()
    }
  }

  def extendExpiration(question_id: Long, extend_expiration_time_sec: Int) =
    DB.withConnection{ implicit c =>
      SQL("UPDATE questions SET expiration_time_sec = expiration_time_sec + {extend_expiration_time_sec} WHERE id = {id}")
        .on('id -> question_id,
          'extend_expiration_time_sec -> extend_expiration_time_sec).executeUpdate()
    }

}