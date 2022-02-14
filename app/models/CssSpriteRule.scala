package models


import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table CssSpriteRules
  *  @param objectId Database column object_id
  *  @param optimizeId Database column optimize_id
  *  @param posX Database column pos_x
  *  @param posY Database column pos_y
  *  @param width Database column width
  *  @param height Database column height  */
case class CssSpriteRule(objectId: Int, optimizeId: Int, posX: Int, posY: Int, width: Int, height: Int)

object CssSpriteRules{
  /** GetResult implicit for fetching CssSpriteRulesRow objects using plain SQL queries */
  implicit def GetResultCssSpriteRule(implicit e0: GR[Int], e1: GR[Option[Int]]): GR[CssSpriteRule] = GR{
    prs => import prs._
      CssSpriteRule.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int]))
  }

  /** Collection-like TableQuery object for table CssSpriteRules */
  lazy val tQuery = new TableQuery(tag => new CssSpriteRules(tag))
}

/** Table description of table css_sprite_rules. Objects of this class serve as prototypes for rows in queries. */
class CssSpriteRules(tag: Tag) extends Table[CssSpriteRule](tag, "css_sprite_rules") {
  def * = (objectId, optimizeId, posX, posY, width, height) <> (CssSpriteRule.tupled, CssSpriteRule.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (objectId.?, optimizeId.?, posX.?, posY.?, width.?, height.?).shaped.<>({r=>import r._; _1.map(_=> CssSpriteRule.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column object_id  */
  val objectId: Column[Int] = column[Int]("object_id")
  /** Database column optimize_id  */
  val optimizeId: Column[Int] = column[Int]("optimize_id")
  /** Database column pos_x  */
  val posX: Column[Int] = column[Int]("pos_x")
  /** Database column pos_y  */
  val posY: Column[Int] = column[Int]("pos_y")
  /** Database column width  */
  val width: Column[Int] = column[Int]("width")
  /** Database column height  */
  val height: Column[Int] = column[Int]("height")

  /** Foreign key referencing OptimizeRules (database name optimizeIdCS) */
  lazy val optimizeRulesFk = foreignKey("optimizeIdCS", optimizeId, OptimizeRules.tQuery)(r => r.optimizeId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  /** Foreign key referencing WebObjects (database name objectIdCS) */
  lazy val webObjectsFk = foreignKey("objectIdCS", objectId, WebContents.tQuery)(r => r.objectId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
}