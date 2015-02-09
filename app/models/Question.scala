package models

import anorm.Pk
import play.api.libs.json.{Json, JsValue, Writes}

/**
 * Created by Mattia on 24.12.2014.
 */

case class Question(id: Pk[Long], question: String, question_type: String, reward_cts: Int, created_at: Long, disabled:
Boolean, expiration_time: Option[Long], maximal_assignments: Option[Int], papers_id: Long)

object Question {
  implicit val questionWrites = new Writes[Question] {
    def writes(q: Question): JsValue = {
      Json.obj(
        "id" -> q.id.get,
        "question" -> q.question,
        "question_type" -> q.question_type,
        "reward" -> q.reward_cts
      )
    }
  }
}