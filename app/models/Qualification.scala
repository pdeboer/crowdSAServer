package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Qualification(id: Pk[Long], questions_id: Long, teams_id: Long)
