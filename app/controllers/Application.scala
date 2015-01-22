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
import persistence.{PaperDAO, JobDAO, QuestionDAO, TurkerDAO}

import play.api.db.DB
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._

import play.api.data.Form
import play.api.data.Forms.{tuple, nonEmptyText}

import scala.util.Try


object Application extends Controller {

  def index = Action { implicit request =>
    val session = request.session
    request.session.get("turkerId").map {
      turkerId =>
        Redirect(routes.Application.waiting())
    } getOrElse {
      Ok(views.html.login())
    }
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

  // POST - Stores a paper in the database, as well as the title, the budget and the uploaded time.
  def storePaper = Action(parse.multipartFormData) { implicit request =>

    request.body.file("source").map { source =>
      val filename = source.filename
      if(filename != "") {
        var title = ""
        var budget = ""
        var contentType = ""
        try {
          title = request.body.asFormUrlEncoded.get("pdfTitle").get(0)
          budget = request.body.asFormUrlEncoded.get("budget").get(0)
          contentType = source.contentType.get
        } catch {
          case e: Exception => InternalServerError("Wrong request structure. pdfTitle, budget or contentType is missing in the request.")
        }

        source.ref.moveTo(new File(s"./public/pdfs/$filename"))
        val paper: Paper = {
          Paper(NotAssigned, "/pdfs/" + filename, title, new Date().getTime, new Integer(budget))
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

  // GET - get all questions
  def questions = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(Json.toJson(QuestionDAO.getAll()))
    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  // GET - get questions related to PDF
  def jobs = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(Json.toJson(JobDAO.getData()))
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

  def waiting = Action { implicit request =>
    val session = request.session

    request.session.get("turkerId").map {
      turkerId =>
        Ok(views.html.waiting(
          TurkerDAO.findByTurkerId(turkerId).getOrElse(null)
        ))
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

  //GET - show pdf viewer and question
  def viewer(questionId: Long) =  Action { implicit request =>

    request.session.get("turkerId").map {
      turkerId =>

        val contentCsv = readCsv(request.session.get("toHighlight").getOrElse(""))
        if (!contentCsv.isEmpty) {
          highlight(contentCsv)
        }

        val question = QuestionDAO.findById(questionId).getOrElse(null)
        Ok(views.html.index(TurkerDAO.findByTurkerId(turkerId).getOrElse(null), question, PaperDAO.findById(question.paper_fk).getOrElse(null)))

    }.getOrElse {
      Redirect(routes.Application.index())
    }
  }

  /**
   * Highlight all the words contained in the contentCsv variable
   * @param contentCsv a List of strings containing all the words/phrases to highlight in the PDF
   */
  def highlight(contentCsv: List[String]) : Unit = {
    val file = "./public/pdfs/test.pdf"
    val parser: PDFParser = new PDFParser(new FileInputStream(file))
    parser.parse()
    val pdDoc: PDDocument = new PDDocument(parser.getDocument)

    val pdfHighlight: TextHighlight = new TextHighlight("UTF-8")
    // depends on what you want to match, but this creates a long string without newlines
    pdfHighlight.setLineSeparator(" ")
    pdfHighlight.initialize(pdDoc)

    for(textRegEx <- contentCsv) {
      pdfHighlight.highlightDefault(textRegEx)
    }
    pdDoc.save("./public/pdfs/demo.pdf")
    try {
      if (parser.getDocument != null) {
        parser.getDocument.close
      }
      if (pdDoc != null) {
        pdDoc.close
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }

  }

  /**
   * Read the CSV file
   * @return
   */
  def readCsv(csv: String) : List[String] = {
    if(csv != ""){
      val res = csv.split(",")
      return res.toList
    } else {
      return List[String]()
    }

    //return Source.fromFile("./public/csv/statTest.csv").getLines().toList
  }
}