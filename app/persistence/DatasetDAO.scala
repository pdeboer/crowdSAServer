package persistence

import anorm.SqlParser._
import anorm._
import models.Dataset
import play.api.Play.current
import play.api.db.DB
/**
 * Created by Mattia on 22.01.2015.
 */
object DatasetDAO {
  private val datasetParser: RowParser[Dataset] =
    get[Pk[Long]]("id") ~
      get[String]("statistical_method") ~
      get[String]("dom_children") ~
      get[String]("name") ~
      get[Option[String]]("url") map {
      case id ~statistical_method ~dom_children ~name ~url=> Dataset(id, statistical_method, dom_children, name, url)
    }

  def findById(id: Long): Option[Dataset] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM datasets WHERE id = {id}").on(
        'id -> id
      ).as(datasetParser.singleOpt)
    }

  /**
   * Add new dataset if it doesn't exists yet
   * @param d
   * @param paperId
   * @return
   */
  def add(d: Dataset, paperId: Long): Long = {

    var id: Option[Long] = None

    val datasets = Datasets2PapersDAO.findDatasetsByPaperId(paperId)
    datasets.foreach(dataset => {
      if(dataset.statistical_method.equalsIgnoreCase(d.statistical_method) && dataset.dom_children.equalsIgnoreCase(d.dom_children)){
        id = Some(dataset.id.get)
      }
    })
    if(id == None) {
      id = DB.withConnection { implicit c =>
          SQL("INSERT INTO datasets(statistical_method, dom_children, name, url) VALUES ({statMethod}, {domChildren}, {name}, {url})").on(
            'statMethod -> d.statistical_method,
            'domChildren -> d.dom_children,
            'name -> d.name,
            'url -> d.url
          ).executeInsert()
        }
    }
    id.get
  }

  def getAll(): List[Dataset] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM datasets").as(datasetParser*).toList
    }
}