package persistence

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
      get[String]("answerType") ~
      get[Int]("reward") ~
      get[Long]("highlight_fk") ~
      get[Long]("paper_fk") map {
      case id ~question ~answerType ~reward ~highlight_fk ~paper_fk => Question(id, question, answerType, reward, highlight_fk, paper_fk)
    }

  def findById(id: Long): Option[Question] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions WHERE id = {id}").on(
      'id -> id
      ).as(questionParser.singleOpt)
    }

  def add(q: Question): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO questions(question, answerType, reward, highlight_fk, paper_fk) VALUES (" +
          "{question}, " +
          "{answerType}, " +
          "{reward}, " +
          "{highlight_fk}, " +
          "{paper_fk})").on(
              'question -> q.question,
              'answerType -> q.answerType,
              'reward -> q.reward,
              'highlight_fk -> q.highlight_fk,
              'paper_fk -> q.paper_fk
          ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Question] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM questions").as(questionParser*)
    }

}