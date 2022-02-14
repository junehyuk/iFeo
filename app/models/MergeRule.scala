package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table MergeRules
  *  @param objectId Database column object_id
  *  @param optimizeId Database column optimize_id
  *  @param index Database column index  */
case class MergeRule(objectId: Int, optimizeId: Int, index: Int)
object MergeRules {
  /** GetResult implicit for fetching MergeRulesRow objects using plain SQL queries */
  implicit def GetResultMergeRule(implicit e0: GR[Int]): GR[MergeRule] = GR{
    prs => import prs._
      MergeRule.tupled((<<[Int], <<[Int], <<[Int]))
  }

  /** Collection-like TableQuery object for table MergeRules */
  lazy val tQuery = new TableQuery(tag => new MergeRules(tag))
}
/** Table description of table merge_rules. Objects of this class serve as prototypes for rows in queries. */
class MergeRules(tag: Tag) extends Table[MergeRule](tag, "merge_rules") {
  def * = (objectId, optimizeId, index) <> (MergeRule.tupled, MergeRule.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (objectId.?, optimizeId.?, index.?).shaped.<>({r=>import r._; _1.map(_=> MergeRule.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column object_id  */
  val objectId: Column[Int] = column[Int]("object_id")
  /** Database column optimize_id  */
  val optimizeId: Column[Int] = column[Int]("optimize_id")
  /** Database column index  */
  val index: Column[Int] = column[Int]("index")

  /** Foreign key referencing OptimizeRules (database name optimizeIdME) */
  lazy val optimizeRulesFk = foreignKey("optimizeIdME", optimizeId, OptimizeRules.tQuery)(r => r.optimizeId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  /** Foreign key referencing WebObjects (database name objectIdME) */
  lazy val webObjectsFk = foreignKey("objectIdME", objectId, WebContents.tQuery)(r => r.objectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
}
