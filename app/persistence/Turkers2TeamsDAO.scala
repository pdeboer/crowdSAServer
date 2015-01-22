package persistence

import java.util.Date
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import models.{Turkers2Teams, Answer}
import play.api.db.DB

/**
  * Created by Mattia on 22.01.2015.
  */
object Turkers2TeamsDAO {
   private val turkers2TeamsParser: RowParser[Turkers2Teams] =
     get[Pk[Long]]("id") ~
       get[Long]("turker_fk") ~
       get[Long]("teams_fk") map {
       case id ~turker_fk ~team_fk => Turkers2Teams(id, turker_fk, team_fk)
     }

   def findById(id: Long): Option[Turkers2Teams] =
     DB.withConnection { implicit c =>
       SQL("SELECT * FROM turkers2teams WHERE id = {id}").on(
         'id -> id
       ).as(turkers2TeamsParser.singleOpt)
     }

   def add(turkerId: Long, teamId: Long): Long = {
     val id: Option[Long] =
       DB.withConnection { implicit c =>
         SQL("INSERT INTO turkers2teams(turker_fk, team_fk) VALUES ({turker_fk}, {team_fk})").on(
           'turker_fk -> turkerId,
           'team_fk -> teamId
         ).executeInsert()
       }
     id.get
   }

   def getAll(): List[Turkers2Teams] =
     DB.withConnection { implicit c =>
       SQL("SELECT * FROM turkers2teams").as(turkers2TeamsParser*).toList
     }
 }
