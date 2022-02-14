package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table Pages
  *  @param pageId Database column page_id PrimaryKey
  *  @param projectId Database column project_id
  *  @param path Database column path
  *  @param header Database column header
  *  @param query Database column query  */
case class Page(pageId: Int, projectId: Int, path: String, header: Option[String], query: Option[String]) {
  def setPageId(id : Int) = new Page(id, projectId, path, header, query)
}
object Pages {
  /** GetResult implicit for fetching PagesRow objects using plain SQL queries */
  implicit def GetResultPage(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]]): GR[Page] = GR{
    prs => import prs._
      Page.tupled((<<[Int], <<[Int], <<[String], <<?[String], <<?[String]))
  }
  /** Collection-like TableQuery object for table Pages */
  lazy val tQuery = new TableQuery(tag => new Pages(tag))
}
/** Table description of table pages. Objects of this class serve as prototypes for rows in queries. */
class Pages(tag: Tag) extends Table[Page](tag, "pages") {
  def * = (pageId, projectId, path, header, query) <> (Page.tupled, Page.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (pageId.?, projectId.?, path.?, header, query).shaped.<>({r=>import r._; _1.map(_=> Page.tupled((_1.get, _2.get, _3.get, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column page_id PrimaryKey */
  val pageId: Column[Int] = column[Int]("page_id", O.AutoInc, O.PrimaryKey)
  /** Database column project_id  */
  val projectId: Column[Int] = column[Int]("project_id")
  /** Database column path  */
  val path: Column[String] = column[String]("path")
  /** Database column header  */
  val header: Column[Option[String]] = column[Option[String]]("header")
  /** Database column query  */
  val query: Column[Option[String]] = column[Option[String]]("query")

  /** Foreign key referencing Projects (database name projectIdPA) */
  lazy val projectsFk = foreignKey("projectIdPA", projectId, Projects.tQuery)(r => r.projectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
}


