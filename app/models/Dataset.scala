package models

import anorm.Pk

/**
 * Created by Mattia on 14.01.2015.
 */

case class Dataset(id: Pk[Long], statMethod: String, domChildren: String, paper_fk: Long)
