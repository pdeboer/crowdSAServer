package persistence

import anorm._
import anorm.SqlParser._
import models.{Dataset, Assignment}
import play.api.db.DB
import play.api.Play.current
/**
 * Created by Mattia on 22.01.2015.
 */
object DatasetDAO {
  private val datasetParser: RowParser[Dataset] =
    get[Pk[Long]]("id") ~
      get[String]("statMethod") ~
      get[String]("domChildred") ~
      get[Long]("paper_fk") map {
      case id ~statMethod ~domChildren ~paper_fk => Dataset(id, statMethod, domChildren, paper_fk)
    }

  def findById(id: Long): Option[Dataset] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM datasets WHERE id = {id}").on(
        'id -> id
      ).as(datasetParser.singleOpt)
    }

  def add(d: Dataset, paperId: Long): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO datasets(statMethod, domChildren, paper_fk) VALUES ({statMethod}, {domChildren}, {paper_fk})").on(
          'statMethod -> d.statMethod,
          'domChildren -> d.domChildren,
          'paper_fk -> paperId
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Dataset] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM datasets").as(datasetParser*).toList
    }
}