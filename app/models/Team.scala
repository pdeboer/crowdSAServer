package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Team(id: Pk[Long], created_at: Long, name: String)
