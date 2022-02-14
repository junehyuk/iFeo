package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table Projects
  *  @param projectId Database column project_id AutoInc, PrimaryKey
  *  @param title Database column title
  *  @param userId Database column user_id
  *  @param domain Database column domain
  *  @param originIp Database column origin_ip
  *  @param proxyuse Database column proxyuse
  *  @param wptTestId Database column wpt_test_id
  *  @param simulTestId Database column simul_test_id  */
case class Project(projectId: Int, title: Option[String], userId: Int, domain: String, originIp: String, proxyuse: Option[Int], wptTestId: Option[String], simulTestId: Option[String])
object Projects {
  /** GetResult implicit for fetching ProjectsRow objects using plain SQL queries */
  implicit def GetResultProject(implicit e0: GR[Int], e1: GR[Option[String]], e2: GR[String], e3: GR[Option[Int]]): GR[Project] = GR{
    prs => import prs._
      Project.tupled((<<[Int], <<?[String], <<[Int], <<[String], <<[String], <<?[Int], <<?[String], <<?[String]))
  }

  /** Collection-like TableQuery object for table Projects */
  lazy val tQuery = new TableQuery(tag => new Projects(tag))
}
/** Table description of table projects. Objects of this class serve as prototypes for rows in queries. */
class Projects(tag: Tag) extends Table[Project](tag, "projects") {
  def * = (projectId, title, userId, domain, originIp, proxyuse, wptTestId, simulTestId) <> (Project.tupled, Project.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (projectId.?, title, userId.?, domain.?, originIp.?, proxyuse, wptTestId, simulTestId).shaped.<>({r=>import r._; _1.map(_=> Project.tupled((_1.get, _2, _3.get, _4.get, _5.get, _6, _7, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column project_id AutoInc, PrimaryKey */
  val projectId: Column[Int] = column[Int]("project_id", O.AutoInc, O.PrimaryKey)
  /** Database column title  */
  val title: Column[Option[String]] = column[Option[String]]("title")
  /** Database column user_id  */
  val userId: Column[Int] = column[Int]("user_id")
  /** Database column domain  */
  val domain: Column[String] = column[String]("domain")
  /** Database column origin_ip  */
  val originIp: Column[String] = column[String]("origin_ip")
  /** Database column proxyuse  */
  val proxyuse: Column[Option[Int]] = column[Option[Int]]("proxyuse")
  /** Database column wpt_test_id  */
  val wptTestId: Column[Option[String]] = column[Option[String]]("wpt_test_id")
  /** Database column simul_test_id  */
  val simulTestId: Column[Option[String]] = column[Option[String]]("simul_test_id")

  /** Foreign key referencing Users (database name userIdPR) */
  lazy val usersFk = foreignKey("userIdPR", userId, Users.tQuery)(r => r.userId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
}