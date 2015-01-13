package controllers

import java.io.FileInputStream
import java.text.DateFormat
import java.util.Date
import anorm.NotAssigned
import models.{Question, Turker}
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import persistence.{PaperDAO, JobDAO, QuestionDAO, TurkerDAO}

import play.api.db.DB
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._

import play.api.data.Form
import play.api.data.Forms.{tuple, nonEmptyText}


object Application extends Controller {

  def index = Action { implicit request =>
    val session = request.session
    request.session.get("turker").map {
      turker =>
        Redirect(routes.Application.waiting())
    } getOrElse {
      Ok(views.html.login())
    }
  }

  def login = Action { implicit request =>

    def turkerId = request.body.asFormUrlEncoded.get("turkerId")(0)
    def email = request.body.asFormUrlEncoded.get("email")(0)

      val turker = TurkerDAO.findByTurkerId(turkerId).getOrElse(null)
      if(turker != null){
        //The turker exists in the database
        if(turker.email.equals(email)) {
          //Check if the email is the same
          TurkerDAO.update(turker.id.get, new Date().getTime)
        } else {
          Redirect(routes.Application.index())
        }
      } else {
        //Create new turker in database
        val d = new Date()
        val res = TurkerDAO.add(
          Turker(NotAssigned, turkerId, email, d.getTime)
        )
      }

    Redirect(routes.Application.waiting()).withSession(("turker" -> turkerId))
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

  def account = Action { implicit request =>
    val session = request.session

    request.session.get("turker").map {
      turker =>
        Ok(views.html.account(TurkerDAO.findByTurkerId(turker).getOrElse(null)))
    }.getOrElse {
      Redirect(routes.Application.login())
    }
  }
  // GET - get all questions
  def questions = Action { implicit request =>
    val session = request.session

    request.session.get("turker").map {
      turker =>
        Ok(Json.toJson(QuestionDAO.getAll()))
    }.getOrElse {
      Redirect(routes.Application.login())
    }
  }

  // GET - get questions related to PDF
  def jobs = Action { implicit request =>
    val session = request.session

    request.session.get("turker").map {
      turker =>
        Ok(Json.toJson(JobDAO.getData()))
    }.getOrElse {
      Redirect(routes.Application.login())
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
        Ok(views.html.login())
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

    request.session.get("turker").map {
      turker =>
        Ok(views.html.waiting(
          TurkerDAO.findByTurkerId(turker).getOrElse(null)
        ))
    }.getOrElse {
      Redirect(routes.Application.login())
    }
  }

  //GET - statistics for user
  def statistics = Action { implicit request =>
    request.session.get("turker").map {
      turker =>
        Ok(views.html.statistic(TurkerDAO.findByTurkerId(turker).getOrElse(null)))
    }.getOrElse {
      Redirect(routes.Application.login())
    }
  }

  //GET - show pdf viewer and question
  def viewer(questionId: Long) =  Action { implicit request =>

      request.session.get("turker").map {
        turker =>

          val contentCsv = readCsv(request.session.get("toHighlight").getOrElse(""))
          if (!contentCsv.isEmpty) {
            highlight(contentCsv)
          }

          val question = QuestionDAO.findById(questionId).getOrElse(null)
          Ok(views.html.index(TurkerDAO.findByTurkerId(turker).getOrElse(null), question, PaperDAO.findById(question.paper_fk).getOrElse(null)))

      }.getOrElse {
        Redirect(routes.Application.login())
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