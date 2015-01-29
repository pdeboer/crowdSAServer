package controllers

import java.io.{File, FileInputStream}
import java.text.DateFormat
import java.util.Date
import javassist.bytecode.stackmap.BasicBlock.Catch
import anorm.NotAssigned
import models.{Paper, Question, Turker}
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDStream
import persistence._
import play.api.Logger

import play.api.db.DB
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._

import play.api.data.Form
import play.api.data.Forms.{tuple, nonEmptyText}
import util.{CSVParser, HighlightPdf}

import scala.util.Try


object Application extends Controller {

  def index = Action { implicit request =>
    val session = request.session
    request.session.get("turkerId").map {
      turkerId =>
        Redirect(routes.Waiting.waiting())
    } getOrElse {
      Ok(views.html.login(request.flash))
    }
  }

  def account = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(views.html.account(TurkerDAO.findByTurkerId(turkerId).getOrElse(null)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  //GET - statistics for user
  def statistics = Action { implicit request =>
    request.session.get("turkerId").map {
      turkerId =>
        Ok(views.html.statistic(TurkerDAO.findByTurkerId(turkerId).getOrElse(null)))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }


}