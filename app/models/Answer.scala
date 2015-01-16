package models

import java.util.Date

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Option[Long],answer: String, answerTime: Long, turkerId: Int, question_fk: Long)