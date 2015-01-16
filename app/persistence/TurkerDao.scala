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
    get[Long]("loginTime") map {
      case id ~turkerId ~email ~loginTime => Turker(id, turkerId, email, loginTime)
    }

  def findById(id: Long): Option[Turker] = {
    DB.withConnection {
      implicit c =>
        SQL("SELECT * FROM turkers WHERE id = {id}").on("id" -> id).as(turkerParser.singleOpt)
    }
  }

  def findByTurkerId(id: String): Option[Turker] = {
    DB.withConnection {
      implicit c =>
        SQL("SELECT * FROM turkers WHERE turkerId = {id}").on('id -> id).as(turkerParser.singleOpt)
    }
  }

  def update(id: Long, loginTime: Long): Long = {
    DB.withConnection {
      implicit c =>
        SQL("UPDATE turkers SET loginTime = {loginTime} WHERE id = {id}").on(
          'loginTime -> loginTime,
          'id -> id
        ).executeUpdate()
        id
    }
  }

  def add(t: Turker): Long = {
    val id: Option[Long] =
      DB.withConnection {
        implicit c =>
          SQL("INSERT INTO turkers(turkerId, email, loginTime) VALUES ({turkerId}, {email}, {loginTime})").on(
            'turkerId -> t.turkerId,
            'email -> t.email,
            'loginTime -> t.loginTime
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

