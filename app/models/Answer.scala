package models

import java.util.Date

/**
 * Created by Mattia on 04.01.2015.
 */

case class Answer(id: Option[Int],answer: String, answerTime: Long, turkerId:Int, qId: Int)