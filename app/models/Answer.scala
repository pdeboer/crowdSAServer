package models

import java.util.Date

import anorm.Pk

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Pk[Long],answer: String, completedTime: Long, accepted: Boolean, acceptedAndBonus: Boolean, rejected: Boolean, question_fk: Long, team_fk: Long)