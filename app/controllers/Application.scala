package controllers

import persistence._
import play.api.mvc._


object Application extends Controller {

  def index = Action { implicit request =>
    val session = request.session
    request.session.get("turkerId").map {
      turkerId =>
        Redirect(routes.Waiting.waiting())
    } getOrElse {
      Ok(views.html.login(request.flash))
    }
  }

  def account = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(views.html.account(TurkerDAO.findByTurkerId(turkerId).getOrElse(null)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  //GET - teams for user
  def teams = Action { implicit request =>
    request.session.get("turkerId").map {
      turkerId =>
        Ok(views.html.teams(TurkerDAO.findByTurkerId(turkerId).getOrElse(null)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }


}