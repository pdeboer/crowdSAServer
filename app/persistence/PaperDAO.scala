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
      get[String]("pdf_path") ~
      get[String]("pdf_title") ~
      get[Long]("created_at") ~
      get[Boolean] ("highlight_enabled") map {
      case id ~pdf_path ~pdf_title ~created_at ~highlight_enabled => Paper(id, pdf_path, pdf_title, created_at, highlight_enabled)
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
        SQL("INSERT INTO papers(pdf_path, pdf_title, created_at, highlight_enabled) VALUES ({pdf_path}, {pdf_title}, {created_at}, {highlight_enabled})").on(
          'pdf_path -> p.pdf_path,
          'pdf_title -> p.pdf_title,
          'created_at -> p.created_at,
          'highlight_enabled -> p.highlight_enabled
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


  def getTitleById(paper_id: Long): String = {
    DB.withConnection { implicit c =>
      findById(paper_id).get.pdf_title
    }
  }

  def getIdByTitle(pdf_title: String) =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM papers WHERE pdf_title = {pdf_title}").on(
        'pdf_title -> pdf_title
      ).as(paperParser.singleOpt).get.id.get
    }

}
