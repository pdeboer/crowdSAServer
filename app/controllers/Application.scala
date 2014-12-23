package controllers

import java.io.FileInputStream

import models.{User, Users}
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.joda.time.{DateTimeZone, DateTime}
import play.api.db.slick.DBAction
import play.api.mvc._


object Application extends Controller {

  def index = DBAction { implicit request =>
    val session = request.dbSession
    request.session.get("connected").map {
      user => Ok("Hello " + user)
        Ok(views.html.waiting(user, Users.list(session)))
    }getOrElse{
      Ok(views.html.login())
    }
  }


  def login = DBAction { implicit request =>
    val session = request.dbSession

    def turkerId = request.body.asFormUrlEncoded.get("turkerId")(0)
    if(turkerId != ""){

      Users.add(User(None, turkerId, DateTime.now(DateTimeZone.UTC)), session)
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

  def waiting = DBAction { implicit request =>
    val session = request.dbSession
    request.session.get("connected").map {
      user => Ok("Hello " + user)
        Ok(views.html.waiting(user, Users.list(session)))
    }.getOrElse {
      Unauthorized("Oops, you are not connected")
    }
  }


  def viewer(title: String, toHighlight: String) =  Action { implicit request =>

      request.session.get("connected").map {
        user => Ok("Hello " + user)

          val contentCsv = readCsv(toHighlight)

          if (!contentCsv.isEmpty) {
            highlight(contentCsv)
          }

          Ok(views.html.index(title, user))

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