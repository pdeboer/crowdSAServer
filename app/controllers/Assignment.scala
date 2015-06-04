package controllers

import persistence.AssignmentDAO
import play.Logger
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
     case e: Exception => {
       e.printStackTrace()
       InternalServerError("Wrong request format.")
     }
   }
  }

  /**
   * GET - get the assignment of a specific answer
   * @param ansId
   * @return
   */
  def assignmentOfAnswer(ansId: Long) = Action { implicit request =>
    try {
      val assignment = AssignmentDAO.findByAnswerId(ansId).get
      Ok(Json.toJson(assignment))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("Error: Cannot get assignment for answer: " + ansId)
      }
    }
  }

  /**
   * GET - check if there is an assignment still open for a turker
   * @param turker_id
   * @return true if exists an open assignment, false otherwise
   */
  def isAssignmentOpen(turker_id: String) = Action {implicit request =>
    try {
      val assignment = AssignmentDAO.getOpenAssignment(turker_id)
      if(assignment!=null){
          val time = assignment.expiration_time

          Ok("{\"assigned\" : \"true\", \"time\": \""+time+"\"}")
      }else {
        Ok("")
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("Error: Cannot check if an assignment is open")
      }
    }

  }

  /**
   * Get - get the url for the open assignment
   * @param turker_id
   * @return redirect to the open assignment
   */
  def getViewerUrlOpenAssignment(turker_id: String) = Action {implicit request =>
    try {
        val assignment = AssignmentDAO.getOpenAssignment(turker_id)
        if(assignment != null) {
          Redirect("/viewer/"+assignment.questions_id+"/"+assignment.id)
        } else {
          Ok("No open questions to answer")
        }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("Error: Cannot get open assignment")
      }
    }

  }

  /**
   * Get - get only Id of open assignment
   * @param turker_id
   * @return id of the assignment, -1 otherwise
   */
  def getOpenAssignmentId(turker_id: String) = Action {
    try {
     val assignment = AssignmentDAO.getOpenAssignment(turker_id)
        if(assignment != null) {
          Ok(assignment.id.get.toString)
        } else {
          Ok("-1")
        }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("Error: Cannot get open assignment id")
      }
    }
  }
}
