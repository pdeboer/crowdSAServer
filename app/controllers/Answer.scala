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
object Answer extends Controller {

  /**
   * GET - get all answers of a question
   * @return
   */
  def answers(qId: Long) = Action { implicit request =>
   try {
     val assignments = AssignmentDAO.findByQuestionId(qId)
     val answers = AnswerDAO.getAllByAssignmentsIds(assignments)
     Ok(Json.toJson(answers))
   } catch {
     case e: Exception => InternalServerError("Wrong request format.")
   }
  }

  /**
   * POST - evaluate answer
   * @return
   */
  def evaluateAnswer = Action(parse.multipartFormData) { implicit request =>
    try {
      val answerId = request.body.asFormUrlEncoded.get("answerId").get.head.toLong
      val accepted = request.body.asFormUrlEncoded.get("accepted").get.head.toBoolean
      val bonus = request.body.asFormUrlEncoded.get("bonus").get.head.toBoolean
      AnswerDAO.evaluateAnswer(answerId, accepted, bonus)
      if(accepted)
        Ok("Answer: " + answerId + " accepted")
      else
        Ok("Answer: " + answerId + " rejected")
    } catch {
      case e: Exception => InternalServerError("Wrong request format.")
    }
  }

  /**
   * POST - stores an answer in the database
   * @return
   */
  def addAnswer = Action(parse.multipartFormData) { implicit request =>
    val questionType = request.body.asFormUrlEncoded.get("questionType").get.head
    val assignmentId = request.body.asFormUrlEncoded.get("assignmentId").get.head.toLong

    var answer = ""
    if (questionType.equals("Boolean")) {
      try {
        val yes = request.body.asFormUrlEncoded.get("true").get.head.toBoolean
        answer = "true"
      } catch {
        case e: Exception => {
          // Try to get no
          try {
            val no = request.body.asFormUrlEncoded.get("false").get.head.toBoolean
            answer = "false"
          } catch {
            case e1: Exception => {
              println("No answer is given for the Boolean question")
              answer = ""
            }
          }
        }
      }
    } else {
      // If not a boolean is expected
      try {
        answer = request.body.asFormUrlEncoded.get("textAnswer").get.head
      } catch {
        case e: Exception => {
          println("Cannot get the answer from the textbox")
          answer = ""
        }
      }
    }
    val answerId = AnswerDAO.add(new Answer(NotAssigned, answer, (new Date()).getTime, null, null, null, assignmentId))
    Redirect(routes.Waiting.waiting()).flashing(
      "success" -> "Answer correctly stored."
    )
  }

}
