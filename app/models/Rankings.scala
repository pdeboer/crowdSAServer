package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Rankings(id: Pk[Long], rank: Integer, answer_fk: Long, ranking_group_fk: Long)
