package controllers

import java.io.{FileInputStream, BufferedWriter, OutputStreamWriter, FileOutputStream}
import java.util
import java.util.regex.Pattern


import com.itextpdf.text.pdf.{PdfName, PdfStamper, PdfReader}
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy.{TextChunk, TextChunkFilter}
import com.itextpdf.text.pdf.parser._
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.{PDPage, PDDocument}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup

import play.api.mvc._

import scala.io.Source

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.viewer)
  }

  def viewer =  Action {

    val contentCsv = readCsv

    highlight(contentCsv)

    Ok(views.html.index("Let’s Do It at My Place Instead? Attitudinal and Behavioral study of Privacy in Client-Side Personalization"))
  }

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

  def readCsv: List[String] = {
    return Source.fromFile("./public/csv/statTest.csv").getLines().toList
  }

}