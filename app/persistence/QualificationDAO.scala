package persistence

import anorm.SqlParser._
import anorm._
import models.{Qualification, Team}
import play.api.Play.current
import play.api.db.DB

import scala.collection.mutable

/**
  * Created by Mattia on 22.01.2015.
  */
object QualificationDAO {
   private val qualificationParser: RowParser[Qualification] =
     get[Pk[Long]]("id") ~
       get[Long]("questions_id") ~
       get[Long]("teams_id") map {
       case id ~questions_id ~teams_id => Qualification(id, questions_id, teams_id)
     }

   def findById(id: Long): Option[Qualification] =
     DB.withConnection { implicit c =>
       SQL("SELECT * FROM qualifications WHERE id = {id}").on(
         'id -> id
       ).as(qualificationParser.singleOpt)
     }

  def findTeamsByQuestionId(questionId: Long): List[Team] = {
    DB.withConnection { implicit c =>
      val qualifications = SQL("SELECT * FROM qualifications WHERE questions_id = {id}").on(
        'id -> questionId
      ).as(qualificationParser *)
      var result : mutable.MutableList[Team] = new mutable.MutableList[Team]
      for(team <- qualifications){
        result += TeamDAO.findById(team.id.get).get
      }
      result.toList
    }
  }

  def findQualificationsByTeamId(teamId: Long): List[Qualification] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM qualifications WHERE teams_id = {teamId}").on(
        'teamId -> teamId
      ).as(qualificationParser*)
    }

  def findSingleTeamByQuestionId(questionId: Long): Team = {
    val teams = findTeamsByQuestionId(questionId)
    var team : Team = null
    teams.foreach { t =>
      if (t.name.equals(questionId)) {
        team = t
      }
    }
    team
  }


   def add(questionId: Long, teamId: Long): Long = {
     val id: Option[Long] =
       DB.withConnection { implicit c =>
         SQL("INSERT INTO qualifications(questions_id, teams_id) VALUES ({questions_id}, {teams_id})").on(
           'questions_id -> questionId,
           'teams_id -> teamId
         ).executeInsert()
       }
     id.get
   }

   def getAll(): List[Qualification] =
     DB.withConnection { implicit c =>
       SQL("SELECT * FROM qualifications").as(qualificationParser*).toList
     }
 }
