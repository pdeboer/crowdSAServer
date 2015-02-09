package models

import anorm.Pk

/**
 * Created by Mattia on 14.01.2015.
 */

case class Datasets2Papers(id: Pk[Long], datasets_id: Long, papers_id: Long)