package controllers

import anorm.NotAssigned
import controllers.Application._
import controllers.Waiting._
import models.Answer
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import persistence._
import play.api.mvc.{Action, Controller}
import util.{HighlightPdf, CSVParser}
import java.util.Date

/**
 * Created by Mattia on 23.01.2015.
 */
object Viewer extends Controller{

  def storeAnswer = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        val questionType = request.body.asFormUrlEncoded.get("questionType")(0)
        val assignmentId = request.body.asFormUrlEncoded.get("assignmentId")(0).toLong

        var answer = ""
        if(questionType.equals("Boolean")) {
          try {
            val yes = request.body.asFormUrlEncoded.get("true")(0).toBoolean
            answer = "true"
          } catch {
            case e: Exception => {
              // Try to get no
              try {
                val no = request.body.asFormUrlEncoded.get("false")(0).toBoolean
                answer= "false"
              } catch {
                case e1: Exception => {
                  println("No answer is given for the Boolean question")
                  answer = ""
                }
              }
            }
          }
        } else {
          // If not a boolean is expected
          try {
            answer = request.body.asFormUrlEncoded.get("textAnswer")(0)
          } catch {
            case e: Exception => {
              println("Cannot get the answer from the textbox")
              answer = ""
            }
          }
        }
        val answerId = AnswerDAO.add(new Answer(NotAssigned, answer, (new Date()).getTime, null, null, null, assignmentId))
        Redirect(routes.Waiting.waiting()).flashing(
          "success" -> "Answer correctly stored."
        )
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  //GET - show pdf viewer and question
  def viewer(questionId: Long, assignmentId: Long) =  Action { implicit request =>

    request.session.get("turkerId").map {
      turkerId =>
        try {
          val paperId = QuestionDAO.findById(questionId).get.paper_fk
          val paper = PaperDAO.findById(paperId).get
          val pdfPath = paper.pdfPath
          val question = QuestionDAO.findById(questionId).getOrElse(null)

          // Highlight paper only is requested by job creator
          if (paper.highlight) {
            var highlights: String = ""
            HighlightDAO.filterByQuestionId(questionId).map(h => highlights= highlights+(h.terms + ","))

            //val contentCsv = CSVParser.readCsv(request.session.get("toHighlight").getOrElse(""))
            val contentCsv = CSVParser.readCsv(highlights)

            //var pdfArrayByte = new Array[Byte](0)
            if (!contentCsv.isEmpty) {
              Ok(views.html.index(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, Base64.encodeBase64String(HighlightPdf.highlight(pdfPath, contentCsv)), AssignmentDAO.findById(assignmentId).get.team_fk, assignmentId))
            } else {
              Ok(views.html.index(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)),AssignmentDAO.findById(assignmentId).get.team_fk, assignmentId))
            }

          } else {
            Ok(views.html.index(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question,  Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)),AssignmentDAO.findById(assignmentId).get.team_fk, assignmentId))
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

}
