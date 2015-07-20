package controllers

import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.binary.Base64
import pdf.HighlightPdf
import persistence._
import play.api.Logger
import play.api.libs.json.Json
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
          if (AnswerDAO.getByAssignmentId(assignmentId).isDefined) {
            Redirect(routes.Waiting.waiting()).flashing(
              "error" -> "You already answered the question."
            )
          } else {
            val paperId = QuestionDAO.findById(questionId).get.papers_id
            val paper = PaperDAO.findById(paperId).get
            val pdfPath = paper.pdf_path
            val question = QuestionDAO.findById(questionId).orNull

            val config = ConfigFactory.load("application.conf")
            val showReward = config.getBoolean("showReward")

            // Highlight paper only if requested
            if (paper.highlight_enabled) {
              var highlightTerms = ""
              var highlightDataset = ""

              HighlightDAO.filterByQuestionId(questionId).map(h => {

                //Add the dataset into a saparated variable in order to identify it from the rest of the terms
                try {
                  highlightDataset = Json.stringify(Json.parse(h.dataset))
                } catch {
                  case e: Exception => highlightDataset = "[]"
                }
                highlightTerms += h.terms
              })

              Logger.debug("Question's datasets to highlight\n" + highlightDataset)
              Logger.debug("Question's terms to highlight\n" + highlightTerms)

              if(question.question_type.equalsIgnoreCase("Missing")){

                val allMatches = (Json.parse(highlightTerms) \\ "matches").toList

                var allTerms = new mutable.MutableList[String]
                allMatches.foreach(a => {
                  // Remove first and last parenthesis "{" "}" as well as the double quotes (")
                  // and then split at all occurrences ( "," )
                  val allMatchesList = a.toString().substring(2, a.toString().length-2).split("\",\"")
                  allMatchesList.foreach(b => {
                    // Extract only the terms to highlight
                    allTerms += b
                  })
                })

                Logger.debug("Loading Missing Question view with matches:\n" + allTerms.toList)

                Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question,
                  Base64.encodeBase64String(HighlightPdf.highlight(pdfPath, allTerms.toList)),
                  AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward,
                  "", highlightTerms, highlightDataset))

              } else {

                var jumpTo = ""
                if (Json.parse(highlightTerms).as[Seq[String]].length > 0){
                  jumpTo = Json.parse(highlightTerms).as[Seq[String]].head
                }
                //Priority to dataset
                if (Json.parse(highlightDataset).as[Seq[String]].length > 0) {
                  jumpTo = Json.parse(highlightDataset).as[Seq[String]].head
                }

                var toHighlight = new mutable.MutableList[String]
                // Highlight terms
                Json.parse(highlightTerms).as[Seq[String]].foreach(a => {
                  toHighlight += a
                })
                //Highlight dataset
                Json.parse(highlightDataset).as[Seq[String]].foreach(a => {
                  toHighlight += a
                })

                if (!highlightTerms.isEmpty) {
                  Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question,
                    Base64.encodeBase64String(HighlightPdf.highlight(pdfPath, toHighlight.toList)),
                    AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward,
                    jumpTo, highlightTerms, highlightDataset))
                } else {
                  Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question,
                    Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)),
                    AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward, jumpTo,
                    highlightTerms, highlightDataset))
                }
              }
            } else {
              Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question,
                Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)),
                AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward, "", "", ""))
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
