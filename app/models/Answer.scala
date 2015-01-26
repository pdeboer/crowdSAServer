package models

import java.util.Date

import anorm.Pk

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Pk[Long],answer: String, completedTime: Long, accepted: Option[Boolean], acceptedAndBonus: Option[Boolean], rejected: Option[Boolean], assignment_fk: Long)