package persistence

import java.util.Date

import anorm._
import anorm.SqlParser._
import models.{Team, Question}
import play.api.db.DB
import play.api.Play.current
/**
 * Created by Mattia on 22.01.2015.
 */
object TeamDAO {

  private val teamParser: RowParser[Team] =
    get[Pk[Long]]("id") ~
      get[Long]("createdAt") ~
      get[String]("name") map {
      case id ~createdAt ~name => Team(id, createdAt, name)
    }

  def findById(id: Long): Option[Team] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM teams WHERE id = {id}").on(
        'id -> id
      ).as(teamParser.singleOpt)
    }

  def add(t: Team): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO teams(createdAt, name) VALUES ({createdAt}, {name})").on(
            'createdAt -> t.createdAt,
            'name -> t.name
          ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Team] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM teams").as(teamParser*)
    }

}