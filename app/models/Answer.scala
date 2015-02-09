package models

import anorm.{NotAssigned, Pk}
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Pk[Long], answer: String, created_at: Long, accepted: Option[Boolean], bonus_cts: Option[Int], rejected: Option[Boolean], assignments_id: Long)

object Answer {
  /*implicit val answerWrites = new Writes[Answer] {
    def writes(a: Answer): JsValue = {
      Json.obj(
        "id" -> a.id.get,
        "answer" -> a.answer,
        "completedTime" -> a.completedTime,
        "accepted" -> a.accepted,
        "acceptedAndBonus" -> a.acceptedAndBonus,
        "rejected" -> a.rejected,
        "assignment_fk" -> a.assignment_fk
      )
    }
  }*/

  def simpleAnswerExtractor(a: Answer): Option[(Long, String, Long, Option[Boolean],Option[Int],Option[Boolean], Long)] =
    Some(a.id.get, a.answer, a.created_at, a.accepted, a.bonus_cts, a.rejected, a.assignments_id)

  implicit val answerWrites: Writes[Answer] = (
    (__ \ "id").write[Long] and
      (__ \ "answer").write[String] and
      (__ \ "created_at").write[Long] and
      (__ \ "accepted").write[Option[Boolean]] and
      (__ \ "bonus_cts").write[Option[Int]] and
      (__ \ "rejected").write[Option[Boolean]] and
      (__ \ "assignments_id").write[Long]
    )(unlift(simpleAnswerExtractor))
}