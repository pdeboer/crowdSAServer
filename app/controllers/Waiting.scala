package controllers

import anorm.NotAssigned
import com.typesafe.config.ConfigFactory
import java.util.Date
import models.Assignment
import persistence._
import play.api.libs.json.Json
import play.api.mvc._

/**
 * Created by Mattia on 22.01.2015.
 */
object Waiting extends Controller{
  val config = ConfigFactory.load("application.conf")
  val layoutMode = config.getInt("layoutMode")

  def getWaitingLayout(turkerId: String, flash: Flash): Result = {
    try {
      val mode = layoutMode match {
        case 1 => Ok(views.html.waiting_1(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), flash))
        case 2 => Ok(views.html.waiting_2(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), flash))
        case 3 => Ok(views.html.waiting_3(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), flash))
        case 4 => Ok(views.html.waiting_4(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), flash))
        case 5 => Ok(views.html.waiting_5(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), flash))
        case _ => Ok("LayoutMode is not yes defined")
      }
      return mode
    } catch {
      case e: Exception => e.printStackTrace()
    }
    return InternalServerError("Something went wrong!")
  }

  // GET - waiting page
  def waiting = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId => {
        //println("Get Waiting Layout with args: " + request.session.get("success").get)
        getWaitingLayout(turkerId, request.flash)
      }
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  def getQuestion() = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        //TODO: Get an available question from the DB and create an assignment
        val m = layoutMode match {
          case 1 => getRandomQuestionRandomPaper(turkerId) //RANDOM_QUESTION_RANDOM_PAPER
          case 2 => getRandomQuestionSamePaper(turkerId, request.session) //RANDOM_QUESTION_SAME_PAPER
          case 3 => getSameQuestionTypeRandomPaper(turkerId, request.session) //SAME_QUESTION_TYPE_RANDOM_PAPER
          case 4 => getSameQuestionTypeSamePaper(turkerId, request.session) //SAME_QUESTION_TYPE_SAME_PAPER
          case _ => InternalServerError("LayoutMode is not yet defined")
        }
        m
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  /**
   * Ony used for layout 5
   * @param questionId
   * @return
   */
  def getDefinedQuestion(questionId: Long) = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        val assignmentId = assignQuestion(questionId, turkerId)
        Ok(questionId.toString+"/"+assignmentId.toString)
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  /**
   * Get only one question type from a single paper
   * @param turkerId
   * @param session
   * @return
   */
  def getSameQuestionTypeSamePaper(turkerId: String, session: Session) : Result = {
    var questionType: String = ""
    var paperId: Long = 0
    if(session.get("paperId").getOrElse("") == ""){
      // If no paper is yet selected, select a random one
      paperId = PaperDAO.getRandomPaperId()
    } else {
      paperId = session.get("paperId").get.toLong
    }
    if(session.get("questionType").getOrElse("") == ""){
      // If no question type is yet selected, select a random one
      questionType = QuestionDAO.getRandomQuestionType(turkerId)
    } else {
      questionType = session.get("questionType").get
    }
    try {
      val questionId = QuestionDAO.getSameQuestionTypeRandomPaper(turkerId, questionType)
      val assignmentId = assignQuestion(questionId, turkerId)

      Ok(questionId.toString+"/"+assignmentId.toString).withSession(
        session +
          ("paperId" -> paperId.toString) +
          ("questionType" -> questionType)
      )
    } catch {
      case e: Exception => {
        Thread.sleep(5000)
        getSameQuestionTypeSamePaper(turkerId, session)
      }
    }
  }


  /**
   * Get only one question type from random papers
   * @param turkerId
   * @param session
   * @return
   */
  def getSameQuestionTypeRandomPaper(turkerId: String, session: Session) : Result = {
    var questionType: String = ""
    if(session.get("questionType").getOrElse("") == ""){
      // If no question type is yet selected, select a random one
      questionType = QuestionDAO.getRandomQuestionType(turkerId)
    } else {
      questionType = session.get("questionType").get
    }
    try {
      val questionId = QuestionDAO.getSameQuestionTypeRandomPaper(turkerId, questionType)
      val assignmentId = assignQuestion(questionId, turkerId)
      Ok(questionId.toString + "/" + assignmentId.toString).withSession(
        session + ("questionType" -> questionType))
    } catch {
    case e: Exception => {
      Thread.sleep(5000)
      getSameQuestionTypeRandomPaper(turkerId, session)
    }
  }
  }

  /**
   * Get a random questionId from a single paper
   * @param turkerId
   * @return
   */
  def getRandomQuestionSamePaper(turkerId: String, session: Session) : Result = {
    var paperId : Long = 0
    if(session.get("paperId").getOrElse(0) == 0){
      // If no paper is yet selected, select a random paper
       paperId = PaperDAO.getRandomPaperId()
    } else {
      paperId = session.get("paperId").get.toLong
    }
    try {
      val questionId = QuestionDAO.getRandomQuestionSamePaper(turkerId, paperId)
      val assignmentId = assignQuestion(questionId, turkerId)
      Ok(questionId.toString + "/" + assignmentId.toString).withSession(
        session + ("paperId" -> paperId.toString))
    } catch {
      case e: Exception => {
        Thread.sleep(5000)
        getRandomQuestionSamePaper(turkerId, session)
      }
    }
  }

  /**
   * Get a random questionId from the available questions stored in the database
   * @return
   */
  def getRandomQuestionRandomPaper(turkerId: String) : Result = {
    try {
      val questionId = QuestionDAO.getRandomQuestionRandomPaper(turkerId)
      val assignmentId = assignQuestion(questionId, turkerId)
      Ok(questionId.toString+"/"+assignmentId.toString)
    } catch {
      case e: Exception => {
        Thread.sleep(5000)
        getRandomQuestionRandomPaper(turkerId)
      }
    }
  }

  /**
   * Assign a question to a team composed only by 1 turker (the requester)
   * @param questionId
   * @return
   */
  def assignQuestion(questionId: Long, turkerId: String) : Long = {
    val date = (new Date()).getTime
    val timeout = config.getInt("reservationTimeForQuestion")
    val assignment = new Assignment(NotAssigned, date, date + timeout, date, questionId, Turkers2TeamsDAO.findSingleTeamByTurkerId(turkerId).id.get)
    val assignmentId = AssignmentDAO.add(assignment)
    assignmentId
  }

  // GET - get questions related to PDF
  def jobs = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(Json.toJson(JobDAO.getData(turkerId)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  def rejectAssignment = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        val assignmentId = request.body.asJson.get \ "assignmentId"
        AssignmentDAO.remove(assignmentId.toString().replace("\"", "").toLong)
        Ok("")
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }
}
