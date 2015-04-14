package controllers

import persistence.QualificationDAO
import play.api.mvc.{Action, Controller}

/**
 * Created by mattia on 02.04.15.
 */
object Qualification extends Controller{
  /**
   * Post - Create qualification
   * @return confirmation string
   */
  def addQualification = Action(parse.multipartFormData) { implicit request =>
    try {
      val question_id = request.body.asFormUrlEncoded.get("question_id").get.head.toLong
      val teams = request.body.asFormUrlEncoded.get("teams").get.head.split(",")

      teams.foreach(f => {
        QualificationDAO.add(question_id, f.toLong)
      })

      Ok(request.body.asFormUrlEncoded.get("teams").get.head + " cannot answer question: " + question_id)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("Error: Cannot add qualification. Check the parameters.")
      }
    }
  }

}
