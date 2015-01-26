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
      get[Long]("paper_fk") map {
      case id ~question ~questionType ~reward ~createdAt ~paper_fk => Question(id, question, questionType, reward, createdAt, paper_fk)
    }

  def findById(id: Long): Option[Question] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE id = {id}").on(
      'id -> id
      ).as(questionParser.singleOpt)
    }

  def getRandomQuestionRandomPaper: Long = {
    DB.withConnection { implicit c =>
      val randomQuestion = SQL("SELECT * FROM questions GROUP BY RAND() LIMIT 1")
        .as(questionParser.singleOpt)
      randomQuestion.get.id.get
    }
  }

  def add(q: Question): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO questions(question, questionType, reward, createdAt, paper_fk) VALUES (" +
          "{question}, " +
          "{questionType}, " +
          "{reward}, " +
          "{cretedAt}, " +
          "{paper_fk})").on(
              'question -> q.question,
              'questionType -> q.questionType,
              'reward -> q.reward,
              'createdAt -> (new Date()).getTime,
              'paper_fk -> q.paper_fk
          ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Question] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions").as(questionParser*)
    }

  def getRandomQuestionSamePaper(paperId: Long): Long =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE paper_fk = {paperId} GROUP BY RAND() LIMIT 1")
        .on('paperId -> paperId)
        .as(questionParser.single).id.get
    }

  def getSameQuestionTypeRandomPaper(questionType: String): Long = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE questionType = {questionType} GROUP BY RAND() LIMIT 1")
        .on('questionType -> questionType)
        .as(questionParser.single).id.get
    }
  }

  def getRandomQuestionType(): String = {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions GROUP BY RAND() LIMIT 1")
        .as(questionParser.single).questionType
    }
  }

}