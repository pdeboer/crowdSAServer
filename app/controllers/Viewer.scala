package controllers

import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.binary.Base64
import pdf.HighlightPdf
import persistence._
import play.api.mvc.{Action, Controller}

import scala.collection.mutable

/**
 * Created by Mattia on 23.01.2015.
 */
object Viewer extends Controller{

  /** GET - show pdf viewer and question
    * @param questionId question id to load
    * @param assignmentId assignment id for the task
    * @return Redirect to pdf Viewer page
    */
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
            val showReward = config.getBoolean("showReward")

            // Highlight paper only if requested
            if (paper.highlight_enabled) {
              val highlights: mutable.MutableList[String] = new mutable.MutableList[String]
              HighlightDAO.filterByQuestionId(questionId).map(h => {
                h.terms.split("#").map(term => {
                  //Highlight only the words that contain more than 2 characters and are not empty spaces
                  // Words like or, an, to, in .. won't be highlighted if selected on their own.
                  if(term != "" & term != " " && term.length > 2){
                    highlights += term
                  }
                })
              })

              val contentCsv = highlights.toList

              var jumpTo = ""
              if(highlights.length>0 && question.question_type == "Discovery"){
                jumpTo = question.question.substring(question.question.indexOf("<i> ") + 4, question.question.indexOf("</i>")-1)
              }else if(highlights.length>0) {
                jumpTo = highlights.head
              }

              if (!contentCsv.isEmpty) {
                Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, Base64.encodeBase64String(HighlightPdf.highlight(pdfPath, highlights.toList)), AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward, jumpTo, highlights.mkString("#")))
              } else {
                Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)), AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward, jumpTo, highlights.mkString("#")))
              }
            } else {
              Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)), AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward, "", ""))
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

  /**
   * Get - Cancel the assignment because of timeout or explicitly requested
   * @param assignmentId id of assignment to be cancelled
   * @return Redirect to questions overview if there exists more, to paper overview if there is no more question to answer
   */
  def cancel(assignmentId: Long) = Action {
    AssignmentDAO.cancel(assignmentId)
    val paperId = QuestionDAO.findById(AssignmentDAO.findById(assignmentId).get.questions_id).get.papers_id
    Redirect(routes.Waiting.secondStep(paperId))
  }
}
