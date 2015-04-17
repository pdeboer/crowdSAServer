package controllers

import anorm.NotAssigned
import models.Dataset
import persistence._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Created by mattia on 18.03.15.
 */
object Dataset extends Controller {

  /**
   * GET - Get all available dataset for a paper Id
   * @param paperId if of the paper
   */
  def getAllByPaperId(paperId: Long) = Action { implicit request =>
    try {

      val datasets = Datasets2PapersDAO.findDatasetsByPaperId(paperId)
      Ok(Json.toJson(datasets))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("Wrong request format.")
      }
    }
  }

  /**
   * POST - Create new dataset if it doesn't exists yet
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

      val dataset_id = DatasetDAO.add(new Dataset(NotAssigned, stat_method, answer.answer, answer.hashCode().toString, Some("")), paper_id)

      Datasets2PapersDAO.add(dataset_id, paper_id)

      Ok(dataset_id.toString)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("-1")
      }
    }
  }

  def getDataset(datasetId: Long) = Action { implicit request =>
    try {
      val dataset = DatasetDAO.findById(datasetId)
      Ok(Json.toJson(dataset))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        InternalServerError("Wrong request format.")
      }
    }
  }

}
