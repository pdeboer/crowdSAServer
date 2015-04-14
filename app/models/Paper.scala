package models

import anorm.Pk
import play.api.libs.json.{JsValue, Json, Writes}

/**
 * Created by Mattia on 26.12.2014.
 */

case class Paper(id: Pk[Long], pdf_path: String, pdf_title: String, created_at: Long, highlight_enabled: Boolean)

object Paper {
  implicit val questionWrites = new Writes[Paper] {
    def writes(p: Paper): JsValue = {
      Json.obj(
        "id" -> p.id.get,
        "pdf_title" -> p.pdf_title
      )
    }
  }
}