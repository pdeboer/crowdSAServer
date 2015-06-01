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
              val dataset_highlight: mutable.MutableList[String] = new mutable.MutableList[String]

              HighlightDAO.filterByQuestionId(questionId).map(h => {

                //Add the dataset into a saparated variable in order to identify it from the rest of the terms
                h.dataset.split("#").map(ds => {
                  if(ds != "" & ds != " " && ds.length > 2) {
                    dataset_highlight += ds
                  }
                })

                // If Missing question skip this step (already parsed as JSON)[{method:'xx', matches:['yy', 'cc', ...]}]
                if(question.question_type.equalsIgnoreCase("Missing")){
                  highlights += h.terms
                } else {
                  h.terms.split("#").map(term => {
                    //Highlight only the words that contain more than 2 characters and are not empty spaces
                    // Words like or, an, to, in .. won't be highlighted if selected on their own.
                    if (term != "" & term != " " && term.length > 2) {
                      highlights += term
                    }
                  })
                }
              })

              if(question.question_type.equalsIgnoreCase("Missing")){

                Logger.debug("Parting JSON")
                try{
                  Json.parse(highlights.head)
                }catch{
                  case e:Exception => e.printStackTrace()
                }

                val allMethods = (Json.parse(highlights.head) \\ "matches").toList
                Logger.debug(allMethods.toString)

                var allMatches = new mutable.MutableList[String]
                allMethods.foreach(a => {
                  val all = a.toString.substring(2, a.toString.length-2).split("\",\"")
                  all.foreach(b => {
                      allMatches += b
                  })
                })

                Logger.debug("Load Missing Question view with: " + allMatches.toList)

                Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question,
                  Base64.encodeBase64String(HighlightPdf.highlight(pdfPath, allMatches.toList)),
                  AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward,
                  "", highlights.head.toString, dataset_highlight.mkString("#")))

              } else {

                var jumpTo = ""
                if (highlights.length > 0 && question.question_type == "Discovery") {
                  jumpTo = highlights.head
                  //jumpTo = question.question.substring(question.question.indexOf("<i> ") + 4, question.question.indexOf("</i>")-1)
                } else if (dataset_highlight.length > 0 && question.question_type != "Discovery" && question.question_type != "Missing") {
                  jumpTo = dataset_highlight.head
                } else {
                  jumpTo = ""
                }

                if (!highlights.toList.isEmpty) {
                  Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question,
                    Base64.encodeBase64String(HighlightPdf.highlight(pdfPath, highlights.toList)),
                    AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward,
                    jumpTo, highlights.mkString("#"), dataset_highlight.mkString("#")))
                } else {
                  Ok(views.html.viewer(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question,
                    Base64.encodeBase64String(HighlightPdf.getPdfAsArrayByte(pdfPath)),
                    AssignmentDAO.findById(assignmentId).get.teams_id, assignmentId, showReward, jumpTo,
                    highlights.mkString("#"), dataset_highlight.mkString("#")))
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
