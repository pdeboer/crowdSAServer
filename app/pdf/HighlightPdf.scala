package pdf

import java.io.{ByteArrayOutputStream, FileInputStream}

import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import play.api.Logger

/**
 * Created by Mattia on 22.01.2015.
 */
object HighlightPdf {

  /**
   * Highlight all the words contained in the contentCsv variable
   * @param contentCsv a List of strings containing all the words/phrases to highlight in the PDF
   */
  def highlight(pdfPath: String, contentCsv: List[String]) : Array[Byte] = {
    val file = "./public"+pdfPath
    val parser: PDFParser = new PDFParser(new FileInputStream(file))
    parser.parse()
    val pdDoc: PDDocument = new PDDocument(parser.getDocument)

    val pdfHighlight: TextHighlight = new TextHighlight("UTF-8")
    // depends on what you want to match, but this creates a long string without newlines
    pdfHighlight.setLineSeparator(" ")
    pdfHighlight.initialize(pdDoc)

    for(textRegEx <- contentCsv) {
      Logger.debug("Highlighting: " + textRegEx)
      pdfHighlight.highlightDefault(textRegEx)
    }

    val byteArrayOutputStream = new ByteArrayOutputStream()
    try {
     if (pdDoc != null) {
       pdDoc.save(byteArrayOutputStream)
       pdDoc.close()
     }
     if (parser.getDocument != null) {
       parser.getDocument.close
     }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
    byteArrayOutputStream.toByteArray()
  }

  /**
   * Convert pdf to Array[Byte] object
   * @param pdfPath
   * @return
   */
  def getPdfAsArrayByte(pdfPath: String) : Array[Byte] = {
    val file = "./public"+pdfPath
    val parser: PDFParser = new PDFParser(new FileInputStream(file))
    parser.parse()
    val pdDoc: PDDocument = new PDDocument(parser.getDocument)
    val byteArrayOutputStream = new ByteArrayOutputStream()
    try {
      if (pdDoc != null) {
        pdDoc.save(byteArrayOutputStream)
        pdDoc.close()
      }
      if (parser != null && parser.getDocument != null) {
        parser.getDocument.close
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
    byteArrayOutputStream.toByteArray()
  }
}
