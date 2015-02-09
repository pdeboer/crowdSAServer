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
      get[String]("statistical_method") ~
      get[String]("dom_children") ~
      get[String]("name") ~
      get[Option[String]]("url") map {
      case id ~statistical_method ~dom_children ~name ~url=> Dataset(id, statistical_method, dom_children, name, url)
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
        SQL("INSERT INTO datasets(statistical_method, dom_children, name, url) VALUES ({statMethod}, {domChildren}, {name}, {url})").on(
          'statMethod -> d.statistical_method,
          'domChildren -> d.dom_children,
          'name -> d.name,
          'url -> d.url
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Dataset] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM datasets").as(datasetParser*).toList
    }
}