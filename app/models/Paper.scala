package models

import java.util.Date

import anorm.Pk
import org.joda.time.DateTime

/**
 * Created by Mattia on 26.12.2014.
 */

case class Paper(id: Pk[Long], pdfPath: String, pdfTitle: String, createdAt: Long, budget: Int, highlight: Boolean)