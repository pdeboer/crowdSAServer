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
      get[String]("dataset") ~
      get[Long]("questions_id") map {
      case id ~assumption ~terms ~dataset ~questions_id => Highlight(id, assumption, terms, dataset, questions_id)
    }

  def findById(id: Long): Option[Highlight] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM highlights WHERE id = {id}").on(
        'id -> id
      ).as(highlightParser.singleOpt)
    }

  def filterByQuestionId(questionId: Long): List[Highlight] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM highlights WHERE questions_id = {questionId}").on(
        'questionId -> questionId
      ).as(highlightParser*).toList
    }

  def add(h: Highlight): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO highlights(assumption, terms, dataset, questions_id) VALUES ({assumption}, {terms}, {dataset}, {questions_id})").on(
          'assumption-> h.assumption,
          'terms-> h.terms,
          'dataset-> h.dataset,
          'questions_id -> h.questions_id
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Highlight] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM highlights").as(highlightParser*).toList
    }
}