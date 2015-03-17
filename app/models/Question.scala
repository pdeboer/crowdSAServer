package models

import anorm.Pk
import play.api.libs.json.{Json, JsValue, Writes}

/**
 * Created by Mattia on 24.12.2014.
 */

case class Question(id: Pk[Long], question: String, question_type: String, reward_cts: Int, created_at: Long, disabled:
Boolean, expiration_time_sec: Option[Long], maximal_assignments: Option[Int], papers_id: Long, possible_answers: Option[String])

object Question {
  implicit val questionWrites = new Writes[Question] {
    def writes(q: Question): JsValue = {
      Json.obj(
        "id" -> q.id.get,
        "question" -> q.question,
        "question_type" -> q.question_type,
        "disabled" -> q.disabled,
        "expiration_time_sec" -> q.expiration_time_sec,
        "maximal_assignments" -> q.maximal_assignments,
        "reward" -> q.reward_cts,
        "possible_answers" -> q.possible_answers
      )
    }
  }
}