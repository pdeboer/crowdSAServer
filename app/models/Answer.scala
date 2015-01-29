package models

import anorm.{NotAssigned, Pk}
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Pk[Long],answer: String, completedTime: Long, accepted: Option[Boolean], acceptedAndBonus: Option[Boolean], rejected: Option[Boolean], assignment_fk: Long)

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

  def simpleAnswerExtractor(a: Answer): Option[(Long, String, Long, Option[Boolean],Option[Boolean],Option[Boolean], Long)] =
    Some(a.id.get, a.answer, a.completedTime, a.accepted, a.acceptedAndBonus, a.rejected, a.assignment_fk)

  implicit val answerWrites: Writes[Answer] = (
    (__ \ "id").write[Long] and
      (__ \ "answer").write[String] and
      (__ \ "completedTime").write[Long] and
      (__ \ "accepted").write[Option[Boolean]] and
      (__ \ "acceptedAndBonus").write[Option[Boolean]] and
      (__ \ "rejected").write[Option[Boolean]] and
      (__ \ "assignment_fk").write[Long]
    )(unlift(simpleAnswerExtractor))
}