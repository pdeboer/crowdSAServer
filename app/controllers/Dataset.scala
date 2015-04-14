package controllers

import anorm.NotAssigned
import models.Dataset
import persistence._
import play.api.mvc.{Action, Controller}

/**
 * Created by mattia on 18.03.15.
 */
object Dataset extends Controller {

  /**
   * POST - Create new dataset
   * @return id of the new dataset, -1 otherwise
   */
  def addDataset() = Action(parse.multipartFormData) { implicit request =>
    val answer_id = request.body.asFormUrlEncoded.get("answer_id").get.head.toLong

    try {
      val answer = AnswerDAO.findById(answer_id).get
      val question_id = AssignmentDAO.findByAnswerId(answer_id).get.questions_id
      val question = QuestionDAO.findById(question_id).get.question
      // FIXME: ugly
      val stat_method = question.substring(question.indexOf("<i> ") + 4, question.indexOf(" </i>"))
      val paper_id = PaperDAO.findByAnswerId(answer_id)

      val dataset_id = DatasetDAO.add(new Dataset(NotAssigned, stat_method, answer.answer.replace("#", ","), answer.hashCode().toString, Some("")), paper_id)

      Datasets2PapersDAO.add(dataset_id, paper_id)

      Ok(dataset_id.toString)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("-1")
      }
    }
  }

}
