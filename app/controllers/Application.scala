package controllers

import models.Viewer
import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.viewer)
  }

  def viewer =  Action {
    Ok(views.html.index("CrowdWeaver: Visually Managing Complex Crowd Work"))
  }

}