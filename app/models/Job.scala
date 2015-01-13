package models

import anorm.Pk
import anorm.SqlParser._
import play.api.libs.json.{Json, JsValue, Writes}

/**
 * Created by Mattia on 13.01.2015.
 */

case class Job(questionId: Long, pdfTitle: String, questionType: String, rewardAnswer: Int)

object Job {
  implicit val jobWrites = new Writes[Job] {
    def writes(j: Job): JsValue = {
      Json.obj(
        "questionId" -> j.questionId,
        "pdfTitle" -> j.pdfTitle,
        "questionType" -> j.questionType,
        "rewardAnswer" -> j.rewardAnswer
      )
    }
  }
}