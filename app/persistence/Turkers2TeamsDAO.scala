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
       get[Long]("turker_fk") ~
       get[Long]("team_fk") map {
       case id ~turker_fk ~team_fk => Turkers2Teams(id, turker_fk, team_fk)
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
      val turkers2teams = SQL("SELECT * FROM turkers2teams WHERE turker_fk = {id}").on(
        'id -> id
      ).as(turkers2TeamsParser *)
      var result : mutable.MutableList[Team] = new mutable.MutableList[Team]
      for(team <- turkers2teams){
        result += TeamDAO.findById(team.id.get).get
      }
      result.toList
    }
  }

  def findTurkersByTeamId(teamId: Long): List[Turkers2Teams] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM turkers2teams WHERE team_fk = {teamId}").on(
        'teamId -> teamId
      ).as(turkers2TeamsParser*)
    }

  def findSingleTeamByTurkerId(turkerId: String): Team = {
    val teams = findTeamsByTurkerId(turkerId)
    var team : Team = null
    teams.foreach { t =>
      if (t.name.equals(turkerId)) {
        team = t
      }
    }
    team
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
