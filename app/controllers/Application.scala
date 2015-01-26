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

  // POST - Stores a paper in the database, as well as the title, the budget and the uploaded time.
  def storePaper = Action(parse.multipartFormData) { implicit request =>

    request.body.file("source").map { source =>
      val filename = source.filename
      if(filename != "") {
        var title = ""
        var budget = ""
        var contentType = ""
        var highlight = false
        try {
          title = request.body.asFormUrlEncoded.get("pdfTitle").get(0)
          budget = request.body.asFormUrlEncoded.get("budget").get(0)
          highlight = request.body.asFormUrlEncoded.get("highlight").get(0).toBoolean
          contentType = source.contentType.get
        } catch {
          case e: Exception => InternalServerError("Wrong request structure. pdfTitle, budget or contentType is missing in the request.")
        }

        source.ref.moveTo(new File(s"./public/pdfs/$filename"))
        val paper: Paper = {
          Paper(NotAssigned, "/pdfs/" + filename, title, new Date().getTime, new Integer(budget), highlight)
        }
        val id = PaperDAO.add(paper)

        Ok(id.toString)
      }else {
        InternalServerError("Wrong request! Filename is not valid.")
      }
    }.getOrElse {
      InternalServerError("Wrong request! The source file is missing.")
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



  /*
    def getQuestionWithHitId(id: Int) = DBAction { implicit request =>
      val session = request.dbSession
      val l : List[Question] = Questions.findByHitId(id, session)
      Ok("")
    }

    // GET - get single question
    def getQuestion(id: Int) = DBAction { implicit request =>
      val session = request.dbSession

      request.session.get("turker").map {
        turker =>
          val q: List[Question] = Questions.findByQuestionId(id, session)
          Ok(q(0).question)
      }.getOrElse {
        Ok(views.html.index())
      }
    }

    // POST - stores a hit
    def hit() = DBAction(parse.tolerantJson) { request =>
      val session = request.dbSession

      // Extract hit
      val hitJson : JsValue = request.body \ "hit"
      val hitId : Option[Int] = hitJson.asOpt[Int]
      val pdf : String = (request.body \ "pdf").as[String]
      val hitReceived : DateTime = DateTime.now(DateTimeZone.UTC)
      val hit : Hit = Hit(hitId, pdf, hitReceived)

      Hits.add(hit, session)

      Ok("")

      // TODO: Only HTTPS post allowed with certificate authentication

    }

    // POST - stores a question
    def question() = DBAction(parse.tolerantJson) { request =>
      val session = request.dbSession

      // Extract all questions related to the hit
      val questionsJson : JsValue = request.body \ "question"
      val qId : Option[Int] = questionsJson.asOpt[Int]
      val q : String = (request.body \ "question").as[String]
      val answerType : String = (request.body \ "answerType").as[String]
      val hitFk : Int = (request.body \ "hit_fk").as[Int]

      val question: Question = Question(qId, q, answerType, hitFk)
      Questions.add(question, session)

      Ok("")
      // TODO: Only HTTPS post allowed with certificate authentication
    }
  */

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