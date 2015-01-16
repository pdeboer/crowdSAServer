package models

/**
 * Created by Mattia on 14.01.2015.
 */

case class Dataset(id: Option[Long], statMethod: String, domChildren: String, question_fk: Long)
