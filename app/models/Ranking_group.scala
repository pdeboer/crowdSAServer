package models

import anorm.Pk

/**
 * Created by Mattia on 22.01.2015.
 */
case class Ranking_group(id: Pk[Long], groupName: String)