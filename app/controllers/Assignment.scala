package controllers

import persistence.AssignmentDAO
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Created by Mattia on 29.01.2015.
 */
object Assignment extends Controller {

  /**
   * GET - get all assignments of a question
   * @return
   */
  def assignments(qId: Long) = Action { implicit request =>
   try {
     val assignments = AssignmentDAO.findByQuestionId(qId)
     Ok(Json.toJson(assignments))
   } catch {
     case e: Exception => InternalServerError("Wrong request format.")
   }
  }

  def assignmentOfAnswer(ansId: Long) = Action { implicit request =>
    try {
      val assignment = AssignmentDAO.findByAnswerId(ansId).get
      Ok(Json.toJson(assignment))
    } catch {
      case e: Exception => InternalServerError("Error: Cannot get assignment for answer: " + ansId)
    }
  }

  def isAssignmentOpen(turker_id: String) = Action {implicit request =>
    try {
      val resp = AssignmentDAO.isAnAssignmentAlreadyOpen(turker_id)
      if(resp){
        val time = AssignmentDAO.getOpenAssignment(turker_id).expiration_time

        Ok("{\"assigned\" : "+resp+", \"time\": \""+time+"\"}")
      }else {
        Ok("{\"assigned\" : "+resp+", \"time\": \"0\"}")
      }

    } catch {
      case e: Exception => InternalServerError("Error: Cannot check if an assignment is open")
    }

  }

  def getViewerUrlOpenAssignment(turker_id: String) = Action {implicit request =>
    try {
      if(AssignmentDAO.isAnAssignmentAlreadyOpen(turker_id)){
        val assignment = AssignmentDAO.getOpenAssignment(turker_id)
        if(assignment != null) {
          Redirect("/viewer/"+assignment.questions_id+"/"+assignment.id)
        } else {
          Ok("Assignment not found")
        }
      } else {
        Ok("No open questions to answer")
      }
    } catch {
      case e: Exception => InternalServerError("Error: Cannot get open assignment")
    }

  }

  def getOpenAssignmentId(turker_id: String) = Action {
    try {
      if(AssignmentDAO.isAnAssignmentAlreadyOpen(turker_id)){
        val assignment = AssignmentDAO.getOpenAssignment(turker_id)
        if(assignment != null) {
          Ok(assignment.id.get.toString)
        } else {
          Ok("-1")
        }
      } else {
        Ok("-1")
      }
    } catch {
      case e: Exception => InternalServerError("Error: Cannot get open assignment id")
    }
  }
}
