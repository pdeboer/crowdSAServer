package models

import anorm.Pk
import play.api.libs.json._

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Pk[Long], answer: String, motivation: Option[String], created_at: Long, is_method_used: Boolean,
                  accepted: Option[Boolean], bonus_cts: Option[Int], rejected: Option[Boolean], assignments_id: Long)

object Answer {
  implicit val answerWrites = new Writes[Answer] {
    def writes(a: Answer): JsValue = {
      Json.obj(
        "id" -> a.id.get,
        "answer" -> a.answer,
        "motivation" -> a.motivation,
        "created_at" -> a.created_at,
        "is_method_used" -> a.is_method_used,
        "accepted" -> a.accepted,
        "bonus_cts" -> a.bonus_cts,
        "rejected" -> a.rejected,
        "assignments_id" -> a.assignments_id
      )
    }
  }
}