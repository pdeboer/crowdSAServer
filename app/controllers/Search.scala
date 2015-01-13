package controllers

/**
 * Created by Mattia on 11.01.2015.
 */

import models.Result
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import scala.collection.mutable.ListBuffer
import java.io.File

object Search extends Controller {

  // Simple action - return search results as Json
  def perform(term:String) = Action {
    val m = Result.find(term)
    Ok(Json.toJson(m))
  }
}