package controllers

import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.binary.Base64
import persistence._
import play.api.mvc.{Action, Controller}
import util.HighlightPdf

import scala.collection.mutable

/**
 * Created by Mattia on 23.01.2015.
 */
object Viewer extends Controller{

  //GET - show pdf viewer and question
  def viewer(questionId: Long, assignmentId: Long) =  Action { implicit request =>

    request.session.get("turkerId").map {
      turkerId =>
        try {
          if (AnswerDAO.getByAssignmentId(assignmentId) != None) {
            Redirect(routes.Waiting.waiting()).flashing(
              "error" -> "You already answered the question."
            )
          } else {
            val paperId = QuestionDAO.findById(questionId).get.papers_id
            val paper = PaperDAO.findById(paperId).get
            val pdfPath = paper.pdf_path
            val question = QuestionDAO.findById(questionId).getOrElse(null)

            val config = ConfigFactory.load("application.conf")
            val enablePdfHeader = config.getBoolean("showRewardAndTimer")

            // Highlight paper only if requested by job creator
            if (paper.highlight_enabled) {
              val highlights: mutable.MutableList[String] = new mutable.MutableList[String]
              HighlightDAO.filterByQuestionId(questionId).map(h => {
                h.terms.split(",").map(f => {
                  //Highlight only the words that contain more than 2 characters and are not empty spaces
                  if(f != "" & f != " " && f.length > 2){
                    highlights += f
                  }
                })
              })

              //val contentCsv = CSVParser.readCsv(request.session.get("toHighlight").getOrElse(""))
              val contentCsv = highlights.toList //CSVParser.readCsv(highlights)

              //var pdfArrayByte = new Array[Byte](0)
              if (!contentCsv.isEmpty) {
                Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, Base64.encodeBase64String(HighlightPdf.highlight(pdfPath, highlights.toList)), AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, enablePdfHeader))
              } else {
                Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)), AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, enablePdfHeader))
              }

            } else {
              Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)), AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, enablePdfHeader))
            }
          }
        } catch {
          case e: Exception => {
            e.printStackTrace()
            Redirect(routes.Waiting.waiting()).flashing(
              "error" -> "You cannot answer this question because it cannot be assigned to you right now."
            )
          }
        }
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  def cancel(assignmentId: Long) = Action {
    AssignmentDAO.cancel(assignmentId)
    Redirect(routes.Waiting.waiting())
  }
}
