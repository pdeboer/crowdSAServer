package persistence

import java.sql.Connection
import java.util.Date
import anorm._
import SqlParser._
import com.fasterxml.jackson.annotation.JsonValue
import models.Turker
import play.api.db.DB

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.Play.current


object TurkerDAO {

  private val turkerParser: RowParser[Turker] =
    get[Pk[Long]]("id") ~
      get[String]("turker_id") ~
      get[Option[String]]("email") ~
      get[Long]("login_time") ~
      get[String]("username") ~
      get[String]("password") ~
      get[Int]("layout_mode") map {
      case id ~turker_id ~email ~login_time ~username ~password ~layout_mode => Turker(id, turker_id, email, login_time, username, password, layout_mode)
    }

  def authenticate(username: String, password: String): String = {
    DB.withConnection {
      implicit c =>
        val turker = SQL("SELECT * FROM turkers WHERE username = {username} AND password = {password} LIMIT 1")
          .on('username -> username,
            'password -> password)
          .as(turkerParser.singleOpt)
        try {
          val t = turker.get
          return t.turker_id
        } catch {
          case e: Exception => "Cannot find turker with the corresponding credentials."
        }
        return null
    }
    return null
  }

  def getTotalEarned(turkerId: String): Double = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      val count =
        SQL("SELECT SUM(q.reward_cts) as res FROM questions AS q WHERE EXISTS (SELECT * FROM assignments AS a WHERE teams_id = {teamId} AND questions_id = q.id AND EXISTS( SELECT * FROM answers WHERE accepted=true AND a.id = assignments_id ))")
          .on('teamId -> teamId)
          .apply().head
      try{
        val res = count[BigDecimal]("res")
        res.toInt
      } catch {
        case e: Exception => return 0
      }
    }
  }

  def getTotalBonus(turkerId: String) : Int = {
    val teamId = Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get.toString
    DB.withConnection { implicit c =>
      val count =
        SQL("SELECT SUM(bonus_cts) FROM answers AS a WHERE accepted = true AND EXISTS (SELECT * FROM assignments WHERE a.assignments_id = id AND teams_id = {teamId})")
          .on('teamId -> teamId)
          .apply().head
      try {
        val res = count[Long]("COUNT(*)")
        res.toInt
      } catch {
        case e: Exception => return 0
      }
    }
  }

  /**
   * Returns true if the registration can take place with these variables, otherwise false
   * @param username
   * @param turker_id
   * @return
   */
  def checkRegistration(username: String, turker_id: String): Boolean ={
    val tUsername = findByUsername(username)
    val tTurkerId = findByTurkerId(turker_id)
    try {
      if (tUsername.get != null || tTurkerId.get != null) {
        return false
      }
      return true
    } catch {
      case e: Exception => return true
    }
  }

  def updateLoginTime(turker_id: String, login_time: Long): String = {
    DB.withConnection {
      implicit c =>
        SQL("UPDATE turkers SET login_time = {login_time} WHERE turker_id = {turker_id}").on(
          'login_time -> login_time,
          'turker_id -> turker_id
        ).executeUpdate()
        println("LoginTime updated for turker: " + turker_id)
        turker_id
    }
  }

  def findById(id: Long): Option[Turker] = {
    DB.withConnection {
      implicit c =>
        SQL("SELECT * FROM turkers WHERE id = {id}").on("id" -> id).as(turkerParser.singleOpt)
    }
  }

  def findByUsername(username: String): Option[Turker] = {
    DB.withConnection {
      implicit c =>
        SQL("SELECT * FROM turkers WHERE username = {username}").on('username -> username).as(turkerParser.singleOpt)
    }
  }

  def findByTurkerId(turker_id: String): Option[Turker] = {
    DB.withConnection {
      implicit c =>
        SQL("SELECT * FROM turkers WHERE turker_id = {turker_id}").on('turker_id -> turker_id).as(turkerParser.singleOpt)
    }
  }

  def create(t: Turker): Option[Long] = {
    val id: Option[Long] =
      DB.withConnection {
        implicit c =>
          SQL("INSERT INTO turkers(turker_id, email, login_time, username, password, layout_mode) " +
            "VALUES ({turker_id}, {email}, {login_time}, {username}, {password}, {layout_mode})").on(
              'turker_id -> t.turker_id,
              'email -> t.email,
              'login_time -> t.login_time,
              'username -> t.username,
              'password -> t.password,
              'layout_mode -> t.layout_mode
            ).executeInsert()
      }
    id
  }

  def countAll(): Int = {
    DB.withConnection {
      implicit c =>
        val count = SQL("SELECT COUNT(*) as count FROM turkers").apply().head
        try {
          val res = count[Long]("count")
          res.toInt
        } catch {
          case e: Exception => return 0
        }
    }
  }

}

