package models

/**
 * Created by Mattia on 11.01.2015.
 */

import java.io.File

import persistence.{JobDAO, QuestionDAO}

case class Result(text: String)

// Finds files in the current dir. matching the given search term
object Result {

  // Simple list of files in the current directory
  def all = JobDAO.getData()

  // Simple case-sensitive filter
  def find(term: String) = Result.all.filter(
    j => (
      j.pdfTitle.toLowerCase.contains(term.toLowerCase)
        || j.questionType.toLowerCase.contains(term.toLowerCase)
        || j.rewardAnswer.toString.contains(term)
      )
  )
}
