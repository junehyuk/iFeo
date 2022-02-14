package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}


/** Entity class storing rows of table Dependencies
  *  @param optimizeId Database column optimize_id
  *  @param dependentObject Database column dependent_object  */
case class Dependency(optimizeId: Int, dependentObject: Int)
object Dependencies {
  /** GetResult implicit for fetching DependenciesRow objects using plain SQL queries */
  implicit def GetResultDependency(implicit e0: GR[Int]): GR[Dependency] = GR{
    prs => import prs._
      Dependency.tupled((<<[Int], <<[Int]))
  }
  /** Collection-like TableQuery object for table Dependencies */
  lazy val tQuery = new TableQuery(tag => new Dependencies(tag))
}
/** Table description of table dependencies. Objects of this class serve as prototypes for rows in queries. */
class Dependencies(tag: Tag) extends Table[Dependency](tag, "dependencies") {
  def * = (optimizeId, dependentObject) <> (Dependency.tupled, Dependency.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (optimizeId.?, dependentObject.?).shaped.<>({r=>import r._; _1.map(_=> Dependency.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column optimize_id  */
  val optimizeId: Column[Int] = column[Int]("optimize_id")
  /** Database column dependent_object  */
  val dependentObject: Column[Int] = column[Int]("dependent_object")

  /** Foreign key referencing OptimizeRules (database name optimizeIdDE) */
  lazy val optimizeRulesFk = foreignKey("optimizeIdDE", optimizeId, OptimizeRules.tQuery)(r => r.optimizeId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  /** Foreign key referencing WebObjects (database name objectIdDE) */
  lazy val webObjectsFk = foreignKey("objectIdDE", dependentObject, WebContents.tQuery)(r => r.objectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
}

