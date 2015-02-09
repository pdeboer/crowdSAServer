package models

import java.util.Date

import anorm.Pk
import persistence.{AnswerDAO, QuestionDAO, TurkerDAO}

/**
 * Created by Mattia on 22.12.2014.
 */

case class Turker(id: Pk[Long], turker_id: String, email: Option[String], login_time: Long, username: String, password: String, layout_mode: Int)

object Turker{
  def getRank(t: Turker): String ={
    AnswerDAO.getAcceptedAnswers(t.turker_id).toString()+"/"+TurkerDAO.countAll()
  }

  def earnedSoFar(t: Turker): String ={
    (TurkerDAO.getTotalEarned(t.turker_id)/100).toString()+" $ BONUS: "+(TurkerDAO.getTotalBonus(t.turker_id)/100).toString()+" $"
  }
}


