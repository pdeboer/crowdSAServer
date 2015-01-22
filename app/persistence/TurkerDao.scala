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
      get[String]("turkerId") ~
      get[String]("email") ~
      get[Long]("loginTime") ~
      get[String]("username") ~
      get[String]("password") ~
      get[Int]("layoutMode") map {
      case id ~turkerId ~email ~loginTime ~username ~password ~layoutMode => Turker(id, turkerId, email, loginTime, username, password, layoutMode)
    }

  def authenticate(username: String, password: String): String = {
    DB.withConnection {
      implicit c =>
        val turker = SQL("SELECT * FROM turkers WHERE username = {username} AND password = {password}")
          .on('username -> username, 'password -> password)
          .as(turkerParser.singleOpt)
        try {
          val t = turker.get
          t.turkerId
        } catch {
          case e: Exception => ""
        }
    }
  }

  def updateLoginTime(turkerId: String, loginTime: Long): String = {
    DB.withConnection {
      implicit c =>
        SQL("UPDATE turkers SET loginTime = {loginTime} WHERE turkerId = {turkerId}").on(
          'loginTime -> loginTime,
          'turkerId -> turkerId
        ).executeUpdate()
        turkerId
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

  def findByTurkerId(turkerId: String): Option[Turker] = {
    DB.withConnection {
      implicit c =>
        SQL("SELECT * FROM turkers WHERE turkerId = {turkerId}").on('turkerId -> turkerId).as(turkerParser.singleOpt)
    }
  }

  def create(t: Turker): Long = {
    val id: Option[Long] =
      DB.withConnection {
        implicit c =>
          SQL("INSERT INTO turkers(turkerId, email, loginTime, username, password, layoutMode) " +
            "VALUES ({turkerId}, {email}, {loginTime}, {username}, {password}, {layoutMode})").on(
              'turkerId -> t.turkerId,
              'email -> t.email,
              'loginTime -> t.loginTime,
              'username -> t.username,
              'password -> t.password,
              'layoutMode -> t.layoutMode
            ).executeInsert()
      }
    id.get
  }

/*
  implicit object TurkerFormat extends Format[Turker] {

    // convert from Turker object to JSON (serializing to JSON)
    def writes(t: Turker): JsValue = {
      val stockSeq = Seq(
        "id" -> JsNumber(t.id.get),
        "turkerId" -> JsString(t.turkerId),
        "loginTime" -> JsString(t.loginTime.toString))
      JsObject(stockSeq)
    }

    // convert from a JSON string to a Turker object (de-serializing from JSON)
    def reads(json: JsValue): JsResult[Turker] = {
      val id = (json \ "id").as[Pk[Long]]
      val symbol = (json \ "turkerId").as[String]
      val companyName = (json \ "loginTime").as[Date]
      JsSuccess(Turker(id, symbol, companyName))
    }

  }
  */

}

