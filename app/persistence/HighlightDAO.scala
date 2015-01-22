package persistence

import java.util.Date

import anorm._
import anorm.SqlParser._
import models.{Highlight, Answer}
import play.api.db.DB
import play.api.Play.current
/**
 * Created by Mattia on 22.01.2015.
 */
object HighlightDAO {
  private val highlightParser: RowParser[Highlight] =
    get[Pk[Long]]("id") ~
      get[String]("assumption") ~
      get[String]("terms") ~
      get[Long]("question_fk") map {
      case id ~assumption ~terms ~question_fk => Highlight(id, assumption, terms, question_fk)
    }

  def findById(id: Long): Option[Highlight] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM highlights WHERE id = {id}").on(
        'id -> id
      ).as(highlightParser.singleOpt)
    }

  def filterByQuestionId(questionId: Long): List[Highlight] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM highlights WHERE question_fk = {questionId}").on(
        'questionId -> questionId
      ).as(highlightParser*).toList
    }

  def add(h: Highlight, questionId: Long): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO highlights(assumption, terms, question_fk) VALUES ({assumption}, {terms}, {question_fk})").on(
          'assumption-> h.assumption,
          'terms-> h.terms,
          'question_fk -> questionId
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Highlight] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM highlights").as(highlightParser*).toList
    }
}