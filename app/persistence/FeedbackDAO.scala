package persistence

/**
 * Created by Mattia on 22.01.2015.
 */
package persistence

import java.util.Date

import anorm.SqlParser._
import anorm._
import models.{Feedback, Answer}
import play.api.db.DB
import play.api.Play.current
/**
 * Created by Mattia on 22.01.2015.
 */
object FeedbackDAO {
  private val feedbackParser: RowParser[Feedback] =
    get[Pk[Long]]("id") ~
      get[Int]("useful") ~
      get[Long]("answers_id") map {
      case id ~useful ~answers_id=> Feedback(id, useful, answers_id)
    }

  def findById(id: Long): Option[Feedback] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM feedbacks WHERE id = {id}").on(
        'id -> id
      ).as(feedbackParser.singleOpt)
    }

  def add(r: Feedback): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO feedbacks(useful, answers_id) VALUES ({useful}, {answers_id})").on(
          'useful -> r.useful,
          'answers_id -> r.answers_id
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Feedback] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM feedbacks").as(feedbackParser*).toList
    }
}
