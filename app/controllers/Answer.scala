package controllers

import java.util.Date

import anorm.NotAssigned
import models.Answer
import persistence.{AnswerDAO, AssignmentDAO, PaperDAO}
import play.api.Logger
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
    Logger.debug("Storing answer")
    val question_type = request.body.asFormUrlEncoded.get("question_type").get.head
    val assignments_id = request.body.asFormUrlEncoded.get("assignments_id").get.head.toLong

    if (AnswerDAO.getByAssignmentId(assignments_id) != None) {
      Logger.debug("Cannot store answer. User already answered this question!")
      Redirect(routes.Waiting.waiting()).flashing(
        "error" -> "You already answered the question."
      )
    }
    else {
      val accuracy: String = request.body.asFormUrlEncoded.get("accuracy").getOrElse(Seq[String]("0")).head.toString
      var answer = "["
      val motivation = "[\""+request.body.asFormUrlEncoded.get("motivation[]").getOrElse(Seq[String]("")).mkString("\",\"")+"\"]"
      val observation = "[\""+request.body.asFormUrlEncoded.get("observation").getOrElse(Seq[String]("")).mkString("\",\"")+"\"]"

      //Extract answer from boolean question
      if (question_type.equalsIgnoreCase("Boolean")) {
        Logger.debug("Found question type boolean")
        try {
          val answerElem = request.body.asFormUrlEncoded.get("answer").get.head

          if (answerElem.equalsIgnoreCase("YES")) {
            answer += "\"true\"]"
          } else {
            answer += "\"false\"]"
          }
        } catch {
          case e: Exception => {
            Logger.error("No answer is given for the Boolean question")
            answer += "]"
            e.printStackTrace()
          }
        }
      }
      // Extract answer from voting question
      else if (question_type.equalsIgnoreCase("Voting")) {
        Logger.debug("Found question type voting")
        try {
          val answerParsed = request.body.asFormUrlEncoded.get("answer").get.head
          Logger.debug("Answer VOTING question: ")
          Logger.debug(answerParsed)

          if(answerParsed.equalsIgnoreCase("There exist no dataset for this method")){
            answer += "]"
          } else {
            answer += "\""+answerParsed.substring(2,answerParsed.length-2)+"\"]"
          }
          Logger.debug("Stored answer: " + answer)
        } catch {
          case e: Exception => {
            Logger.error("Cannot get the answer from the checkboxes")
            answer += "]"
            e.printStackTrace()
          }
        }
      }
      // Extract answer from discovery question
      else if (question_type.equalsIgnoreCase("Discovery")) {
        Logger.debug("Found question type discovery")
        try {
          val l = request.body.asFormUrlEncoded.get("dom_children[]").get.mkString("\",\"")
          answer += "\""+ l + "\"]"
        } catch {
          case e: Exception => {
            Logger.error("Cannot get the answer from dom_children")
            answer += "]"
            e.printStackTrace()
          }
        }
      }
      // Extract answer from Missing question
      else if (question_type.equalsIgnoreCase("Missing")) {
        Logger.debug("Found question type missing")
        try {
          val l = request.body.asFormUrlEncoded.get("missing[]").get.mkString("\",\"")
          answer += "\""+ l + "\"]"
        } catch {
          case e: Exception => {
            Logger.error("Cannot get the answer from missing array")
            answer += "]"
            e.printStackTrace()
          }
        }
      }

      // Other variables which need to be extracted from any question type
      val is_method_used = request.body.asFormUrlEncoded.get("is_method_used").get.head.toBoolean

      Logger.debug("Storing answer:\n" + answer)

      val answerId = AnswerDAO.add(new Answer(NotAssigned, answer, Some(motivation), Some(observation),
        new Date().getTime / 1000, is_method_used, null, null, null, assignments_id, accuracy))
      Logger.debug("Answer stored with id: " + answerId)

      if (answerId != -1) {
        Redirect(routes.Waiting.secondStep(PaperDAO.findByAnswerId(answerId))).flashing(
          "success" -> "Your answer has been stored and will be evaluated by other crowd workers."
        )
      } else {
        Redirect(routes.Waiting.waiting()).flashing(
          "error" -> "Error while storing the answer."
        )
      }
    }
  }

}
