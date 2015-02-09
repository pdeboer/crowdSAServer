package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Feedback(id: Pk[Long], useful: Integer, answers_id: Long)
