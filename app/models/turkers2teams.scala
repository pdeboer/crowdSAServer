package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Turkers2Teams(id: Pk[Long], turker_fk: Long, team_fk: Long)