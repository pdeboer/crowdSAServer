package models

import anorm.Pk
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by Mattia on 22.01.2015.
 */
case class Assignment(id: Pk[Long], created_at: Long, expiration_time: Long, is_cancelled: Boolean, questions_id: Long, teams_id: Long)

object Assignment {
  def simpleAssignmentExtractor(a: Assignment): Option[(Long, Long, Long, Boolean, Long, Long)] =
    Some(a.id.get, a.created_at, a.expiration_time, a.is_cancelled, a.questions_id, a.teams_id)

  implicit val assignmentWrites: Writes[Assignment] = (
    (__ \ "id").write[Long] and
      (__ \ "created_at").write[Long] and
      (__ \ "expiration_time").write[Long] and
      (__ \ "is_cancelled").write[Boolean] and
      (__ \ "questions_id").write[Long] and
      (__ \ "teams_id").write[Long]
    )(unlift(simpleAssignmentExtractor))
}