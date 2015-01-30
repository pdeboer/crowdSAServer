package persistence

import anorm._
import anorm.SqlParser._
import models.{Question, Job, Paper}
import play.api.db.DB

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Mattia on 13.01.2015.
 */
object JobDAO {

  private val jobParser: RowParser[Job] =
    get[Long]("questionId") ~
      get[String]("pdfTitle") ~
      get[String]("questionType") ~
      get[Int]("rewardAnswer") map {
      case questionId ~pdfTitle ~questionType ~rewardAnswer => Job(questionId, pdfTitle, questionType, rewardAnswer)
    }

  def getData(turkerId: String): List[Job] = {
    val papers: List[Paper] = PaperDAO.getAll()
    val questions: List[Question] = QuestionDAO.getAllEnabled(turkerId)
    val a: ArrayBuffer[Job] = new ArrayBuffer[Job]()
    papers.foreach(
      p => {
        questions.filter(
          q =>
            q.paper_fk == p.id.get
        ).foreach(
            qq =>
              a += Job(qq.id.get, p.pdfTitle, qq.questionType, qq.reward)
          )
      }
    )

    a.toList
  }
}
