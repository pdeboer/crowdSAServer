package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Assignment(id: Pk[Long], assignedFrom: Long, assignedTo: Long, acceptedTime: Long, question_fk: Long, team_fk: Long)