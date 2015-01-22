package controllers

import java.util.Date

import anorm.NotAssigned
import controllers.Application._
import models.Turker
import org.apache.commons.codec.digest.DigestUtils
import persistence.TurkerDAO
import play.api.mvc.{Action, Controller}

/**
 * Created by Mattia on 22.01.2015.
 */
object Login extends Controller {

  def login = Action { implicit request =>

    def username = request.body.asFormUrlEncoded.get("username")(0)
    def password = DigestUtils.md5Hex(request.body.asFormUrlEncoded.get("password")(0))

    val turkerId = TurkerDAO.authenticate(username, password)
    if(!turkerId.isEmpty){
      TurkerDAO.updateLoginTime(turkerId, new Date().getTime)
    } else {
      Redirect(routes.Application.index())
    }

    Redirect(routes.Application.waiting()).withSession("turkerId" -> turkerId.toString)
  }
}
