package controllers

import java.io.FileInputStream

import models._
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.joda.time.{DateTime, DateTimeZone}
import play.api.db.slick.DBAction
import play.api.libs.json.JsValue
import play.api.mvc._


object Application extends Controller {

  def index = DBAction { implicit request =>
    val session = request.dbSession
    request.session.get("connected").map {
      turker =>
        Ok(views.html.waiting(turker, Hits.list(session), Questions.list(session)))
    } getOrElse {
      Ok(views.html.login())
    }
  }


  def login = DBAction { implicit request =>
    val session = request.dbSession

    def turkerId = request.body.asFormUrlEncoded.get("turkerId")(0)
    if (turkerId != "") {
      Turkers.add(Turker(None, turkerId, DateTime.now(DateTimeZone.UTC)), session)
      Redirect(routes.Application.waiting()).withSession(
        "connected" -> turkerId)
    } else {
      Unauthorized("Incorrect login!")
    }
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

  // GET - get all questions
  def questions = DBAction { implicit request =>
    val session = request.dbSession

    request.session.get("connected").map {
      turker =>
        Ok(Questions.list(session).size.toString)
    }.getOrElse {
      Unauthorized("Oops, you are not connected")
    }
  }

  def getQuestionWithHitId(id: Int) = DBAction { implicit request =>
    val session = request.dbSession
    val l : List[Question] = Questions.findByHitId(id, session)
    Ok("")
  }

  // GET - get single question
  def getQuestion(id: Int) = DBAction { implicit request =>
    val session = request.dbSession

    request.session.get("connected").map {
      turker =>
        val q: List[Question] = Questions.findByQuestionId(id, session)
        Ok(q(0).question)
    }.getOrElse {
      Unauthorized("Oops, you are not connected")
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

  def waiting = DBAction { implicit request =>
    val session = request.dbSession

    // TODO: Check the database if new questions
    // are available and if there are new
    // questions reload the page

    request.session.get("connected").map {
      turker =>
        Ok(views.html.waiting(turker, Hits.list(session), Questions.list(session)))
    }.getOrElse {
      Unauthorized("Oops, you are not connected")
    }
  }

  // TODO: Add question parameter
  def viewer(title: String, toHighlight: String) =  Action { implicit request =>

      request.session.get("connected").map {
        turker =>

          val contentCsv = readCsv(toHighlight)

          if (!contentCsv.isEmpty) {
            highlight(contentCsv)
          }

          Ok(views.html.index(title, turker))

      }.getOrElse {
        Unauthorized("Oops, you are not connected")
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