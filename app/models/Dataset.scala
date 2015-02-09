package models

import anorm.Pk

/**
 * Created by Mattia on 14.01.2015.
 */

case class Dataset(id: Pk[Long], statistical_method: String, dom_children: String, name: String, url: Option[String])