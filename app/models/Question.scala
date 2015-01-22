package models

import anorm.Pk
import play.api.libs.json.{Json, JsValue, Writes}

/**
 * Created by Mattia on 24.12.2014.
 */

case class Question(id: Pk[Long], question: String, questionType: String, reward: Int, createdAt: Long, paper_fk: Long)

object Question {
  implicit val questionWrites = new Writes[Question] {
    def writes(q: Question): JsValue = {
      Json.obj(
        "id" -> q.id.get,
        "question" -> q.question,
        "questionType" -> q.questionType,
        "reward" -> q.reward,
        "createdAt" -> q.createdAt,
        "paper_fk" -> q.paper_fk
      )
    }
  }
}