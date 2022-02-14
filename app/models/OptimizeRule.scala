package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table OptimizeRules
  *  @param optimizeId Database column optimize_id AutoInc, PrimaryKey
  *  @param projectId Database column project_id
  *  @param optimizeType Database column optimize_type
  *  @param createDate Database column create_date
  *  @param result Database column result  */
case class OptimizeRule(optimizeId: Int, projectId: Int, optimizeType: Int, createDate: java.sql.Timestamp, result: Option[Int])
object OptimizeRules {
  /** GetResult implicit for fetching OptimizeRulesRow objects using plain SQL queries */
  implicit def GetResultOptimizeRule(implicit e0: GR[Int], e1: GR[java.sql.Timestamp]): GR[OptimizeRule] = GR{
    prs => import prs._
      OptimizeRule.tupled((<<[Int], <<[Int], <<[Int], <<[java.sql.Timestamp], <<?[Int]))
  }
  /** Collection-like TableQuery object for table OptimizeRules */
  lazy val tQuery = new TableQuery(tag => new OptimizeRules(tag))
}
/** Table description of table optimize_rules. Objects of this class serve as prototypes for rows in queries. */
class OptimizeRules(tag: Tag) extends Table[OptimizeRule](tag, "optimize_rules") {
  def * = (optimizeId, projectId, optimizeType, createDate, result) <> (OptimizeRule.tupled, OptimizeRule.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (optimizeId.?, projectId.?, optimizeType.?, createDate.?, result).shaped.<>({r=>import r._; _1.map(_=> OptimizeRule.tupled((_1.get, _2.get, _3.get, _4.get, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column optimize_id AutoInc, PrimaryKey */
  val optimizeId: Column[Int] = column[Int]("optimize_id", O.AutoInc, O.PrimaryKey)
  /** Database column project_id  */
  val projectId: Column[Int] = column[Int]("project_id")
  /** Database column optimize_type  */
  val optimizeType: Column[Int] = column[Int]("optimize_type")
  /** Database column create_date  */
  val createDate: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("create_date")
  /** Database column result  */
  val result: Column[Option[Int]] = column[Option[Int]]("result")

  /** Foreign key referencing Projects (database name projectIdOP) */
  lazy val projectsFk = foreignKey("projectIdOP", projectId, Projects.tQuery)(r => r.projectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  /** Foreign key referencing WebObjects (database name objectIdOp) */
  lazy val webObjectsFk = foreignKey("objectIdOp", result, WebContents.tQuery)(r => r.objectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
}


