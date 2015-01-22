package persistence

/**
 * Created by Mattia on 22.01.2015.
 */
package persistence

import java.util.Date

import anorm.SqlParser._
import anorm._
import models.{Rankings, Ranking_group, Answer}
import play.api.db.DB
import play.api.Play.current
/**
 * Created by Mattia on 22.01.2015.
 */
object RankingsDAO {
  private val rankingParser: RowParser[Rankings] =
    get[Pk[Long]]("id") ~
      get[Int]("rank") ~
      get[Long]("answer_fk") ~
      get[Long]("ranking_group_fk") map {
      case id ~rank ~answer_fk ~ranking_group_fk => Rankings(id, rank, answer_fk, ranking_group_fk)
    }

  def findById(id: Long): Option[Rankings] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM rankings WHERE id = {id}").on(
        'id -> id
      ).as(rankingParser.singleOpt)
    }

  def add(r: Rankings): Long = {
    val id: Option[Long] =
      DB.withConnection { implicit c =>
        SQL("INSERT INTO rankings(rank, answer_fk, ranking_group_fk) VALUES ({rank}, {answer_fk}, {ranking_group_fk})").on(
          'rank -> r.rank,
          'answer_fk -> r.answer_fk,
          'ranking_group_fk -> r.ranking_group_fk
        ).executeInsert()
      }
    id.get
  }

  def getAll(): List[Rankings] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM rankings").as(rankingParser*).toList
    }
}
