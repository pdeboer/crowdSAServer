package persistence

import java.util.Date
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import models.{Team, Turkers2Teams, Answer}
import play.api.db.DB

import scala.collection.mutable

/**
  * Created by Mattia on 22.01.2015.
  */
object Turkers2TeamsDAO {
   private val turkers2TeamsParser: RowParser[Turkers2Teams] =
     get[Pk[Long]]("id") ~
       get[Long]("turkers_id") ~
       get[Long]("teams_id") map {
       case id ~turkers_id ~teams_id => Turkers2Teams(id, turkers_id, teams_id)
     }

   def findById(id: Long): Option[Turkers2Teams] =
     DB.withConnection { implicit c =>
       SQL("SELECT * FROM turkers2teams WHERE id = {id}").on(
         'id -> id
       ).as(turkers2TeamsParser.singleOpt)
     }

  def findTeamsByTurkerId(turkerId: String): List[Team] = {
    DB.withConnection { implicit c =>
      val id = TurkerDAO.findByTurkerId(turkerId).get.id.get
      val turkers2teams = SQL("SELECT * FROM turkers2teams WHERE turkers_id = {id}").on(
        'id -> id
      ).as(turkers2TeamsParser *)
      var result : mutable.MutableList[Team] = new mutable.MutableList[Team]
      for(t2t <- turkers2teams){
        result += TeamDAO.findById(t2t.teams_id).get
      }
      result.toList
    }
  }

  def findTurkersByTeamId(teamId: Long): List[Turkers2Teams] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM turkers2teams WHERE teams_id = {teamId}").on(
        'teamId -> teamId
      ).as(turkers2TeamsParser*)
    }

  def findSingleTeamByTurkerId(turkerId: String): Team = {
    var team : Team = null
    findTeamsByTurkerId(turkerId).foreach { t =>
      if (t.name.equals(turkerId)) {
        team = t
      }
    }
    team
  }


   def add(turkerId: Long, teamId: Long): Long = {
     val id: Option[Long] =
       DB.withConnection { implicit c =>
         SQL("INSERT INTO turkers2teams(turkers_id, teams_id) VALUES ({turkers_id}, {teams_id})").on(
           'turkers_id -> turkerId,
           'teams_id -> teamId
         ).executeInsert()
       }
     id.get
   }

   def getAll(): List[Turkers2Teams] =
     DB.withConnection { implicit c =>
       SQL("SELECT * FROM turkers2teams").as(turkers2TeamsParser*).toList
     }
 }
