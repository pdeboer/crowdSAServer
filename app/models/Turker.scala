package models

import anorm.Pk
import persistence.TurkerDAO

/**
 * Created by Mattia on 22.12.2014.
 */

case class Turker(id: Pk[Long], turker_id: String, created_at: Long, email: Option[String], login_time: Long, logout_time: Option[Long], username: String, password: String, layout_mode: Int, feedback: Option[String])

object Turker{
  def getRank(t: Turker): String ={

    val rank = TurkerDAO.getRank(t.turker_id)
    val totalTurkers = TurkerDAO.countAll()
    if(rank == 0){
      totalTurkers + "/" + totalTurkers
    } else {
      rank.toString() + "/" + totalTurkers
    }
  }

  def earnedSoFar(t: Turker): String ={
    ((TurkerDAO.getTotalEarned(t.turker_id) + TurkerDAO.getTotalBonus(t.turker_id))/100).toString()+" $"
  }
}


