package controllers


import anorm.NotAssigned
import models.{Highlight, Question}
import persistence._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Created by Mattia on 22.01.2015.
 */
object Question extends Controller {

  // GET - get all questions
  def questions = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(Json.toJson(QuestionDAO.getAllEnabled(turkerId)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  def addQuestion = Action(parse.multipartFormData) { implicit request =>
    try {
      val question = request.body.asFormUrlEncoded.get("question").get.head
      val question_type = request.body.asFormUrlEncoded.get("question_type").get.head
      val reward_cts = request.body.asFormUrlEncoded.get("reward_cts").get.head.toInt
      val created_at = request.body.asFormUrlEncoded.get("created_at").get.head.toLong
      val papers_id = request.body.asFormUrlEncoded.get("papers_id").get.head.toLong
      val expiration_time_sec = request.body.asFormUrlEncoded.get("expiration_time_sec").get.headOption.getOrElse(null).toInt
      val maximal_assignments = request.body.asFormUrlEncoded.get("maximal_assignments").get.headOption.getOrElse(null).toInt
      val possible_answers = request.body.asFormUrlEncoded.get("possible_answers").get.head

      val ques = new Question(NotAssigned, question, question_type, reward_cts, created_at, false, Some(expiration_time_sec), Some(maximal_assignments), papers_id, Some(possible_answers))
      println(ques.toString)
      val id = QuestionDAO.add(ques)

      Ok(id.toString)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("Error: Cannot add question. Check the parameters.")
      }
    }
  }

  def disableQuestion = Action(parse.multipartFormData) { implicit request =>
    try {
      val question_id = request.body.asFormUrlEncoded.get("question_id").get.head.toLong
      QuestionDAO.disable(question_id)
      Ok("Question successfully disabled!")
    } catch {
      case e: Exception => InternalServerError("Error: Wrong request format.")
    }
  }

  def addHighlight() = Action(parse.multipartFormData) { implicit request =>
    try {
      val questionId = request.body.asFormUrlEncoded.get("questionId").get.head.toLong
      val assumption = request.body.asFormUrlEncoded.get("assumption").get.head
      val terms = request.body.asFormUrlEncoded.get("terms").get.head
      val h = new Highlight(NotAssigned, assumption, terms, questionId)
      HighlightDAO.add(h)
      Ok("Success!")
    } catch {
      case e: Exception => InternalServerError("Wrong request format.")
    }
  }

  def getAllQuestionsByPaperId(paper_id: Long) = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turker_id =>
        Ok(Json.toJson(QuestionDAO.getQuestionsByPaperId(turker_id, paper_id)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  def extendMaxAssignments() = Action(parse.multipartFormData) {implicit request =>
    try {
      val question_id = request.body.asFormUrlEncoded.get("question_id").get.head.toLong
      val maximal_assignments = request.body.asFormUrlEncoded.get("maximal_assignments").get.head.toInt

      QuestionDAO.extendMaxAssignments(question_id, maximal_assignments)

      Ok("Successfully extended maximal assignments for question id: " + question_id)
    } catch {
      case e: Exception => InternalServerError("Cannot extend maximal assignments for this question." +
        " Check the parameters.")
    }
  }

  def extendExpiration() = Action(parse.multipartFormData) {implicit request =>
    try {
      val question_id = request.body.asFormUrlEncoded.get("question_id").get.head.toLong
      val expiration_increment_sec = request.body.asFormUrlEncoded.get("expiration_increment_sec").get.head.toInt

      QuestionDAO.extendExpiration(question_id, expiration_increment_sec)

      Ok("Successfully extended expiration time for question id: " + question_id)
    } catch {
      case e: Exception => InternalServerError("Cannot extend expiration time for this question." +
        " Check the parameters.")
    }
  }
}
