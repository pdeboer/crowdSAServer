package controllers


import anorm.NotAssigned
import models.Question
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
        Ok(Json.toJson(QuestionDAO.getAllEnabled()))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  def addQuestion = Action(parse.multipartFormData) { implicit request =>
    try {
      val question = request.body.asFormUrlEncoded.get("question").get.head
      val questionType = request.body.asFormUrlEncoded.get("questionType").get.head
      val reward = request.body.asFormUrlEncoded.get("reward").get.head.toInt
      val createdAt = request.body.asFormUrlEncoded.get("created").get.head.toLong
      val paper_fk = request.body.asFormUrlEncoded.get("paper_fk").get.head.toLong

      val ques = new Question(NotAssigned, question, questionType, reward, createdAt, false, paper_fk)
      val id = QuestionDAO.add(ques)

      Ok(id.toString)
    }catch {
      case e: Exception => InternalServerError("Cannot add question. Check the parameters.")
    }
  }

  def disableQuestion = Action(parse.multipartFormData) { implicit request =>
    try {
      val questionId = request.body.asFormUrlEncoded.get("questionId").get.head.toLong
      QuestionDAO.cancel(questionId)
      Ok("Question successfully disabled!")
    } catch {
      case e: Exception => InternalServerError("Wrong request format.")
    }
  }
}
