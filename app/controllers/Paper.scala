package controllers

import java.io.File
import java.util.Date

import anorm.NotAssigned
import models.Paper
import persistence.PaperDAO
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Created by Mattia on 22.01.2015.
 */
object Paper extends Controller {
  /**
   * Get - check if paperId exists in database
   * @param paperId
   * @return true if found, false otherwise
   */
  def checkPaper(paperId: Long) = Action { implicit request =>
    try {
      PaperDAO.findById(paperId).get
      Ok("true")
    }catch {
      case e: Exception => InternalServerError("false")
    }
  }

  /** POST - Stores a paper in the database, as well as the title, the budget and the uploaded time.
    * @return id of the new paper stored in the database
    */
  def storePaper = Action(parse.multipartFormData) { implicit request =>

    request.body.file("source").map { source =>
      val filename = source.filename
      if(filename != "") {
        var title = ""
        var budget = ""
        var contentType = ""
        var highlight = false
        try {
          title = request.body.asFormUrlEncoded.get("pdf_title").get(0)
          highlight = request.body.asFormUrlEncoded.get("highlight_enabled").get(0).toBoolean
          contentType = source.contentType.get
        } catch {
          case e: Exception => InternalServerError("Wrong request structure. pdf_title, highlight_enabled or source is missing in the request.")
        }

        //Move pdf to public/pdfs folder
        source.ref.moveTo(new File(s"./public/pdfs/$filename"), true)
        val paper: Paper = new Paper(NotAssigned, "/pdfs/" + filename, title, new Date().getTime/1000, highlight)
        val id = PaperDAO.add(paper)

        Ok(id.toString)
      }else {
        InternalServerError("Wrong request! Filename is not valid.")
      }
    }.getOrElse {
      InternalServerError("Wrong request! The source file is missing.")
    }
  }

  /**
   * Get - get all papers stored in the database
   * @return List of papers
   */
  def getAllPapers() = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        try {
          Ok(Json.toJson(PaperDAO.getAll(turkerId)))
        }catch{
          case e:Exception => {InternalServerError("Cannot get all the papers.")}
        }
    }.getOrElse {
      Redirect(routes.Application.index())
    }

  }

  /**
   * Get - Get paper from an answer.
   * @param answerId
   * @return paper object
   */
  def getPaperIdFromAnswerId(answerId: Long) = Action { implicit request =>
    try {
      Ok(Json.toJson(PaperDAO.findByAnswerId(answerId)))
    }catch{
      case e:Exception => {InternalServerError("Cannot get id of the paper for answer with id: " + answerId)}
    }
  }
}
