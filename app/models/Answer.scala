package models

import anorm.Pk
import play.api.libs.json._

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Pk[Long], answer: String, motivation: Option[String], observation: Option[String], createdAt: Long, isMethodUsed: Boolean,
                  accepted: Option[Boolean], bonusCts: Option[Int], rejected: Option[Boolean], assignmentsId: Long, accuracy: String)

object Answer {
  implicit val answerWrites = new Writes[Answer] {
    def writes(a: Answer): JsValue = {
      Json.obj(
        "id" -> a.id.get,
        "answer" -> a.answer,
        "motivation" -> a.motivation,
        "created_at" -> a.createdAt,
        "is_method_used" -> a.isMethodUsed,
        "accepted" -> a.accepted,
        "bonus_cts" -> a.bonusCts,
        "rejected" -> a.rejected,
        "assignments_id" -> a.assignmentsId,
        "accuracy" -> a.accuracy
      )
    }
  }
}