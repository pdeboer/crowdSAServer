package controllers

import java.util.Date

import anorm.NotAssigned
import com.typesafe.config.ConfigFactory
import controllers.Application._
import models.{Team, Turkers2Teams, Turker}
import org.apache.commons.codec.digest.DigestUtils
import persistence.{TeamDAO, Turkers2TeamsDAO, TurkerDAO}
import play.api.mvc.{Action, Controller}

/**
 * Created by Mattia on 22.01.2015.
 */
object Login extends Controller {

  def login = Action { implicit request =>

    val username = request.body.asFormUrlEncoded.get("username")(0)
    val password = DigestUtils.md5Hex(request.body.asFormUrlEncoded.get("password")(0))
    val turkerId = TurkerDAO.authenticate(username, password)

    if(turkerId != null){
      TurkerDAO.updateLoginTime(turkerId, new Date().getTime/1000)
      Redirect(routes.Waiting.waiting()).withSession("turkerId" -> turkerId)
    } else {
      Redirect(routes.Application.index()).flashing("error" -> "Invalid combination of username/password")
    }
  }

  def registration = Action { implicit request =>
    Ok(views.html.registration(request.flash))
  }

  def addTurker = Action { implicit request =>
    try {
      val username = request.body.asFormUrlEncoded.get("username")(0)

      val password1 = DigestUtils.md5Hex(request.body.asFormUrlEncoded.get("password1")(0))
      val password2 = DigestUtils.md5Hex(request.body.asFormUrlEncoded.get("password2")(0))
      val email = request.body.asFormUrlEncoded.get("email")(0)
      if (password1.equals(password2)) {
        if (TurkerDAO.checkRegistration(username, password1)) {
          val config = ConfigFactory.load("application.conf")
          val layoutMode = config.getInt("layoutMode")

          val turkerId = request.body.asFormUrlEncoded.get("turkerId")(0)
          val turker = new Turker(NotAssigned, turkerId, Some(email), (new Date()).getTime/1000, username, password1, layoutMode)
          val newTurkerId = TurkerDAO.create(turker)
          try {
            val id = newTurkerId.get
            // Automatically create single team!
            val team = new Team(NotAssigned, (new Date()).getTime/1000, turkerId)
            val teamId = TeamDAO.add(team)
            val t2t = Turkers2TeamsDAO.add(id, teamId)
            Redirect(routes.Waiting.waiting()).withSession("turkerId" -> turkerId).flashing("success" -> "New user registered.")
          } catch {
            case e: Exception =>
              Redirect(routes.Login.registration()).flashing("error" -> "Unable to register new user.")
          }
        } else {
          Redirect(routes.Login.registration()).flashing("error" -> "Username or turkerID already registered.")
        }
      } else {
        Redirect(routes.Login.registration()).flashing("error" -> "The passwords are not the same.")
      }
    }catch {
      case e: Exception => Redirect(routes.Login.registration()).flashing("error" -> "Registration incomplete.")
    }
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}
