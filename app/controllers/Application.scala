package controllers

import java.io.{FileInputStream}


import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.{PDDocument}

import play.api.mvc._

import scala.io.Source

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.viewer(""))
  }

  def viewer(toHighlight: String) =  Action {

    val contentCsv = readCsv(toHighlight)

    if(!contentCsv.isEmpty) {
      highlight(contentCsv)
    }

    Ok(views.html.index("Let’s Do It at My Place Instead? Attitudinal and Behavioral study of Privacy in Client-Side Personalization"))
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