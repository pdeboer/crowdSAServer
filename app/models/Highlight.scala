package models

import anorm.Pk

/**
 * Created by Mattia on 14.01.2015.
 */

case class Highlight(id: Pk[Long],assumption: String, terms: String, paper_fk: Long)
