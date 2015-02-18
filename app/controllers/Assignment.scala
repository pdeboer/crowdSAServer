package controllers

import java.util.Date

import anorm.NotAssigned
import models.Answer
import persistence.{AnswerDAO, AssignmentDAO}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Created by Mattia on 29.01.2015.
 */
object Assignment extends Controller {

  /**
   * GET - get all assignments of a question
   * @return
   */
  def assignments(qId: Long) = Action { implicit request =>
   try {
     val assignments = AssignmentDAO.findByQuestionId(qId)
     Ok(Json.toJson(assignments))
   } catch {
     case e: Exception => InternalServerError("Wrong request format.")
   }
  }

  def assignmentOfAnswer(ansId: Long) = Action { implicit request =>
    try {
      val assignment = AssignmentDAO.findByAnswerId(ansId).get
      Ok(Json.toJson(assignment))
    } catch {
      case e: Exception => InternalServerError("Error: Cannot get assignment for answer: " + ansId)
    }
  }
}
