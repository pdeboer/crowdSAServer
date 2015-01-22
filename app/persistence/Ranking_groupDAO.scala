package persistence

import java.util.Date

import anorm.SqlParser._
import anorm._
import models.{Ranking_group, Answer}
import play.api.db.DB
import play.api.Play.current
/**
  * Created by Mattia on 22.01.2015.
  */
object Ranking_groupDAO {
   private val ranking_groupParser: RowParser[Ranking_group] =
     get[Pk[Long]]("id") ~
       get[String]("groupName") map {
       case id ~groupName => Ranking_group(id, groupName)
     }

   def findById(id: Long): Option[Ranking_group] =
     DB.withConnection { implicit c =>
       SQL("SELECT * FROM ranking_groups WHERE id = {id}").on(
         'id -> id
       ).as(ranking_groupParser.singleOpt)
     }

   def add(r: Ranking_group): Long = {
     val id: Option[Long] =
       DB.withConnection { implicit c =>
         SQL("INSERT INTO ranking_groups(groupName) VALUES ({groupName})").on(
           'groupName -> r.groupName
         ).executeInsert()
       }
     id.get
   }

   def getAll(): List[Ranking_group] =
     DB.withConnection { implicit c =>
       SQL("SELECT * FROM ranking_groups").as(ranking_groupParser*).toList
     }
 }
