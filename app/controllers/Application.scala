package controllers

import persistence._
import play.api.Logger
import play.api.mvc._


object Application extends Controller{

  /**
   * GET - index of application
   * @return
   */
  def index = Action { implicit request =>
    val session = request.session
    request.session.get("turkerId").map {
      turkerId =>
        Redirect(routes.Waiting.waiting())
    } getOrElse {
      Ok(views.html.login(request.flash))
    }
  }

  /**
   * Get - Account overview with details
   * @return
   */
  def account = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(views.html.account(TurkerDAO.findByTurkerId(turkerId).getOrElse(null)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  /**
   * Get - Teams overview
   * @return
   */
  def teams = Action { implicit request =>
    request.session.get("turkerId").map {
      turkerId =>
        Ok(views.html.teams(TurkerDAO.findByTurkerId(turkerId).getOrElse(null)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  /**
   *POST - update turker's feedback
   */
  def updateFeedback =  Action { implicit request =>
    Logger.debug("Updating feedback")
    request.session.get("turkerId").map {
      turkerId =>
        val turk = TurkerDAO.findByTurkerId(turkerId).getOrElse(null)
        TurkerDAO.updateFeedback(turk.id.get, request.body.asJson.get.\\("feedback").head.toString())
        Logger.debug("Feedback updated")
        Ok("Feedback successfully updated")
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

}