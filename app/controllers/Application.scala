package controllers

import java.io.FileOutputStream
import java.util

import com.lowagie.text.pdf.parser.{TextRenderInfo, RenderListener, PdfContentStreamProcessor ,PdfTextExtractor}
import com.lowagie.text.{Paragraph, Document}
import com.lowagie.text.pdf.{PdfContentByte, PdfName, PdfReader, PdfWriter}
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.viewer)
  }

  def viewer =  Action {

    //highlight

    Ok(views.html.index("Let’s Do It at My Place Instead? Attitudinal and Behavioral study of Privacy in Client-Side Personalization"))
  }

  def highlight: Unit = {
    // ***** Java iText Library *****

    val reader = new PdfReader("./public/pdfs/test.pdf")

    val processor = new PdfContentStreamProcessor(new RenderListener {

      override def renderText(textRenderInfo: TextRenderInfo): Unit = {
        println("<"+textRenderInfo.getText+">")
      }

      override def reset(): Unit = {
        println(">")
      }
    })

    val pageDic = reader.getPageN(1)
    //val resourcesDic = pageDic.getAsDict(PdfName.RESOURCES)
    processor.processContent(reader.getPageContent(1), pageDic)

    /*var parser = new PdfReaderContentParser(reader)
    for(i <- 1 to reader.getNumberOfPages){
      val strategy = parser.processContent(i, new SimpleTextExtractionStrategy())
      println(strategy.getResultantText())
    }*/

    /*val page1 = util.Arrays.toString(reader.getPageContent(1))
    val readable = new String(reader.getPageContent(1))
    println(readable)*/

    /*val doc = new Document()
    PdfWriter.getInstance(doc, new FileOutputStream("./public/pdfs/result.pdf"))
    doc.open()
    doc.add(new Paragraph("Hello result"))
    doc.close()*/
    // ***** End Java code *****
  }
}