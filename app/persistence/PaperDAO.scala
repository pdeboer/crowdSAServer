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
      get[Int]("budget")~
      get[Boolean] ("highlight") map {
      case id ~pdfPath ~pdfTitle ~createdAt ~budget ~highlight => Paper(id, pdfPath, pdfTitle, createdAt, budget, highlight)
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
        SQL("INSERT INTO papers(pdfPath, pdfTitle, createdAt, budget, highlight) VALUES ({pdfPath}, {pdfTitle}, {createdAt}, {budget}, {highlight})").on(
          'pdfPath -> p.pdfPath,
          'pdfTitle -> p.pdfTitle,
          'createdAt -> p.createdAt,
          'budget -> p.budget,
          'highlight -> p.highlight
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Paper] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM papers").as(paperParser*).toList
    }

  def getRandomPaperId(): Long =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM papers GROUP BY RAND() LIMIT 1").as(paperParser.single).id.get
    }
}
