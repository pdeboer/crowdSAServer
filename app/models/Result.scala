package models

/**
 * Created by Mattia on 11.01.2015.
 */

import java.io.File

import persistence.{JobDAO, QuestionDAO}

case class Result(text: String)

object Result {

  // Simple case-sensitive filter
  def find(turkerId: String, term: String) = JobDAO.getData(turkerId).filter(
    j => (
      j.pdfTitle.toLowerCase.contains(term.toLowerCase)
        || j.questionType.toLowerCase.contains(term.toLowerCase)
        || j.rewardAnswer.toString.contains(term)
      )
  )
}
