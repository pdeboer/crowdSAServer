package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Assignment(id: Pk[Long], created_at: Long, expiration_time: Long, is_cancelled: Boolean, questions_id: Long, teams_id: Long)