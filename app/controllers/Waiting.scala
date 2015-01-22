package controllers

import controllers.Application._
import persistence.{TurkerDAO, JobDAO, QuestionDAO}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Created by Mattia on 22.01.2015.
 */
object Waiting extends Controller{

  // GET - waiting page
  def waiting = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(views.html.waiting(
          TurkerDAO.findByTurkerId(turkerId).getOrElse(null)
        ))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  // GET - get all questions
  def questions = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(Json.toJson(QuestionDAO.getAll()))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  // GET - get questions related to PDF
  def jobs = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(Json.toJson(JobDAO.getData()))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }
}
