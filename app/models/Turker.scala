package models

import java.util.Date

import anorm.Pk
import persistence.{QuestionDAO, TurkerDAO}

/**
 * Created by Mattia on 22.12.2014.
 */

case class Turker(id: Pk[Long], turkerId: String, email: String, loginTime: Long, username: String, password: String, layoutMode: Int)


