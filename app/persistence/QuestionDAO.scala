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

  def getRandomQuestionRandomPaper: Long = {
    DB.withConnection { implicit c =>
      val randomQuestion = SQL("SELECT * FROM questions WHERE disabled=false GROUP BY RAND() LIMIT 1")
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

  def cancel(id: Long) =
    DB.withConnection { implicit c =>
      SQL("UPDATE questions SET disabled = 1 WHERE id = {id}").on(
        'id -> id
      ).executeUpdate()
    }

  def getAllEnabled(): List[Question] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE disabled=false").as(questionParser*)
    }

  def getRandomQuestionSamePaper(paperId: Long): Long =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE paper_fk = {paperId} AND disabled=false GROUP BY RAND() LIMIT 1")
        .on('paperId -> paperId)
        .as(questionParser.single).id.get
    }

  def getSameQuestionTypeRandomPaper(questionType: String): Long = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE questionType = {questionType}  AND disabled=false GROUP BY RAND() LIMIT 1")
        .on('questionType -> questionType)
        .as(questionParser.single).id.get
    }
  }

  def getRandomQuestionType(): String = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions AND disabled=false GROUP BY RAND() LIMIT 1")
        .as(questionParser.single).questionType
    }
  }

}