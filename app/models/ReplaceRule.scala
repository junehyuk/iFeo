package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table ReplaceRules
  *  @param replaceId Database column replace_id PrimaryKey
  *  @param projectId Database column project_id
  *  @param optimizeId Database column optimize_id
  *  @param fromObject Database column from_object
  *  @param destObject Database column dest_object  */
case class ReplaceRule(replaceId: Int, projectId: Int, optimizeId: Int, fromObject: Int, destObject: Int, autoProxy : Boolean= false) {
  def setOptimizeId(optId : Int) = {
    new ReplaceRule(replaceId, projectId, optId, fromObject, destObject, autoProxy)
  }
}

object ReplaceRules {
  /** GetResult implicit for fetching ReplaceRulesRow objects using plain SQL queries */
  implicit def GetResultReplaceRule(implicit e0: GR[Int]): GR[ReplaceRule] = GR{
    prs => import prs._
      ReplaceRule.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[Boolean]))
  }
  /** Collection-like TableQuery object for table ReplaceRules */
  lazy val tQuery = new TableQuery(tag => new ReplaceRules(tag))
}

/** Table description of table replace_rules. Objects of this class serve as prototypes for rows in queries. */
class ReplaceRules(tag: Tag) extends Table[ReplaceRule](tag, "replace_rules") {
  def * = (replaceId, projectId, optimizeId, fromObject, destObject, autoProxy) <> (ReplaceRule.tupled, ReplaceRule.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (replaceId.?, projectId.?, optimizeId.?, fromObject.?, destObject.?, autoProxy.?).shaped.<>({r=>import r._; _1.map(_=> ReplaceRule.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column replace_id PrimaryKey */
  val replaceId: Column[Int] = column[Int]("replace_id", O.PrimaryKey,O.AutoInc)
  /** Database column project_id  */
  val projectId: Column[Int] = column[Int]("project_id")
  /** Database column optimize_id  */
  val optimizeId: Column[Int] = column[Int]("optimize_id")
  /** Database column from_object  */
  val fromObject: Column[Int] = column[Int]("from_object")
  /** Database column dest_object  */
  val destObject: Column[Int] = column[Int]("dest_object")
  /** Database column auto_proxy  */
  val autoProxy: Column[Boolean] = column[Boolean]("auto_proxy")

  /** Foreign key referencing OptimizeRules (database name optimizeIdRE) */
  lazy val optimizeRulesFk = foreignKey("optimizeIdRE", optimizeId, OptimizeRules.tQuery)(r => r.optimizeId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  /** Foreign key referencing Projects (database name projectIdRE) */
  lazy val projectsFk = foreignKey("projectIdRE", projectId, Projects.tQuery)(r => r.projectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  /** Foreign key referencing WebObjects (database name destRE) */
  lazy val webObjectsFk3 = foreignKey("destRE", destObject, WebContents.tQuery)(r => r.objectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  /** Foreign key referencing WebObjects (database name fromRE) */
  lazy val webObjectsFk4 = foreignKey("fromRE", fromObject, WebContents.tQuery)(r => r.objectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
}

