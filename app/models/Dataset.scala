package models

import anorm.Pk
import play.api.libs.json.{JsValue, Json, Writes}

/**
 * Created by Mattia on 14.01.2015.
 */

case class Dataset(id: Pk[Long], statistical_method: String, dom_children: String, name: String, url: Option[String])

object Dataset {
  implicit val datasetWrites = new Writes[Dataset] {
    def writes(a: Dataset): JsValue = {
      Json.obj(
        "id" -> a.id.get,
        "statistical_method" -> a.statistical_method,
        "dom_children" -> a.dom_children,
        "name" -> a.name,
        "url" -> a.url)
    }
  }
}