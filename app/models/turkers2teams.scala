package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Turkers2Teams(id: Pk[Long], turkers_id: Long, teams_id: Long)