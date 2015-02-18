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
     case e: Exception => {
       e.printStackTrace()
       InternalServerError("Wrong request format.")
     }
   }
  }

  /**
   * POST - evaluate answer
   * @return
   */
  def evaluateAnswer = Action(parse.multipartFormData) { implicit request =>
    try {
      val answer_id = request.body.asFormUrlEncoded.get("answer_id").get.head.toLong
      val accepted = request.body.asFormUrlEncoded.get("accepted").get.head.toBoolean
      val bonus_cts = request.body.asFormUrlEncoded.get("bonus_cts").get.head.toInt
      AnswerDAO.evaluateAnswer(answer_id, accepted, Some(bonus_cts))
      if(accepted)
        Ok("Answer: " + answer_id + " accepted")
      else
        Ok("Answer: " + answer_id + " rejected")
    } catch {
      case e: Exception => InternalServerError("Wrong request format.")
    }
  }

  /**
   * POST - stores an answer in the database
   * @return
   */
  def addAnswer = Action(parse.multipartFormData) { implicit request =>
    println("Storing answer")
    val question_type = request.body.asFormUrlEncoded.get("question_type").get.head
    val assignments_id = request.body.asFormUrlEncoded.get("assignments_id").get.head.toLong

    var answer = ""
    if (question_type.equalsIgnoreCase("Boolean")) {
      println("Found question type boolean")
      try {
        val answerElem = request.body.asFormUrlEncoded.get("answer").get.head
        if(answerElem.equalsIgnoreCase("YES")){
          answer = "true"
        } else {
          answer = "false"
        }
      } catch {
        case e: Exception => {
          println("No answer is given for the Boolean question")
          answer = ""
        }
      }
    } else if(question_type.equalsIgnoreCase("FreeText")){
      println("Found question type free text")
      try {
        answer = request.body.asFormUrlEncoded.get("textAnswer").get.head
      } catch {
        case e: Exception => {
          println("Cannot get the answer from the textbox")
          answer = ""
        }
      }
    } else if(question_type.equalsIgnoreCase("Discovery")){
      println("Found question type discovery")
      try {
        val keys = request.body.asFormUrlEncoded.keySet//get("dom_children").get.head
        for(k <- keys){
          if(k.startsWith("dom_children")){
            answer = request.body.asFormUrlEncoded.get(k).get.mkString(",")
          }
        }
      } catch {
        case e: Exception => {
          println("Cannot get the answer from dom_children")
          answer = ""
        }
      }
    }

    val answerId = AnswerDAO.add(new Answer(NotAssigned, answer, new Date().getTime/1000, null, null, null, assignments_id))
    println("Answer stored with id: " + answerId)

    Redirect(routes.Waiting.waiting()).flashing(
      "success" -> "Answer correctly stored."
    )
  }

}
