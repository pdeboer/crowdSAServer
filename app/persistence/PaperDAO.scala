package persistence

import anorm._
import anorm.SqlParser._
import models.{Paper, Turker}
import play.api.db.DB

import play.api.Play.current

/**
 * Created by Mattia on 08.01.2015.
 */
object PaperDAO {

  private val paperParser: RowParser[Paper] =
    get[Pk[Long]]("id") ~
      get[String]("pdfPath") ~
      get[String]("pdfTitle") ~
      get[Long]("createdAt") ~
      get[Int]("budget") map {
      case id ~pdfPath ~pdfTitle ~createdAt ~budget => Paper(id, pdfPath, pdfTitle, createdAt, budget)
    }

  def findById(id: Long): Option[Paper] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM papers WHERE id = {id}").on(
        'id -> id
      ).as(paperParser.singleOpt)
    }

  def add(p: Paper): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO papers(pdfPath, pdfTitle, createdAt, budget) VALUES ({pdfPath}, {pdfTitle}, {createdAt}, {budget})").on(
          'pdfPath -> p.pdfPath,
          'pdfTitle -> p.pdfTitle,
          'createdAt -> p.createdAt,
          'budget -> p.budget
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Paper] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM papers").as(paperParser*).toList
    }

}
