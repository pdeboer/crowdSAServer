package controllers

import java.io.File
import java.util.Date

import anorm.NotAssigned
import controllers.Application._
import controllers.Question._
import models.{Paper, Question}
import persistence.{PaperDAO, QuestionDAO}
import play.api.mvc.{Action, Controller}

/**
 * Created by Mattia on 22.01.2015.
 */
object Paper extends Controller {

  def checkPaper(paperId: Long) = Action { implicit request =>
    try {
      val paper = PaperDAO.findById(paperId).get
      Ok("true")
    }catch {
      case e: Exception => InternalServerError("false")
    }
  }

  // POST - Stores a paper in the database, as well as the title, the budget and the uploaded time.
  def storePaper = Action(parse.multipartFormData) { implicit request =>

    request.body.file("source").map { source =>
      val filename = source.filename
      if(filename != "") {
        var title = ""
        var budget = ""
        var contentType = ""
        var highlight = false
        try {
          title = request.body.asFormUrlEncoded.get("pdfTitle").get(0)
          budget = request.body.asFormUrlEncoded.get("budget").get(0)
          highlight = request.body.asFormUrlEncoded.get("highlight").get(0).toBoolean
          contentType = source.contentType.get
        } catch {
          case e: Exception => InternalServerError("Wrong request structure. pdfTitle, budget or contentType is missing in the request.")
        }

        source.ref.moveTo(new File(s"./public/pdfs/$filename"))
        val paper: Paper = new Paper(NotAssigned, "/pdfs/" + filename, title, new Date().getTime, budget.toInt, highlight)
        val id = PaperDAO.add(paper)

        Ok(id.toString)
      }else {
        InternalServerError("Wrong request! Filename is not valid.")
      }
    }.getOrElse {
      InternalServerError("Wrong request! The source file is missing.")
    }
  }
}
