package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table ProxySettings
  *  @param settingId Database column setting_id AutoInc, PrimaryKey
  *  @param projectId Database column project_id
  *  @param contentEncoding Database column content_encoding
  *  @param jsMinify Database column jsminify
  *  @param cssMinify Database column cssminify
  *  @param replace404 Database column replace404
  *  @param favicon Database column favicon
  *  @param headercacheDay Database column headercache_day
  *  @param headercacheType Database column headercache_type  */
case class ProxySetting(settingId: Int, projectId: Option[Int], contentEncoding: Option[String], jsMinify: Option[Boolean], cssMinify: Option[Boolean], replace404: Option[Boolean], favicon: Option[Int], headercacheDay: Option[String], headercacheType: Option[String])
object ProxySettings {
  /** GetResult implicit for fetching ProxySettingsRow objects using plain SQL queries */
  implicit def GetResultProxySetting(implicit e0: GR[Int], e1: GR[Option[Int]], e2: GR[Option[String]], e3: GR[Option[Boolean]]): GR[ProxySetting] = GR{
    prs => import prs._
      ProxySetting.tupled((<<[Int], <<?[Int], <<?[String], <<?[Boolean], <<?[Boolean], <<?[Boolean], <<?[Int], <<?[String], <<?[String]))
  }
  /** Collection-like TableQuery object for table ProxySettings */
  lazy val tQuery = new TableQuery(tag => new ProxySettings(tag))
}
/** Table description of table proxy_settings. Objects of this class serve as prototypes for rows in queries. */
class ProxySettings(tag: Tag) extends Table[ProxySetting](tag, "proxy_settings") {
  def * = (settingId, projectId, contentEncoding, jsMinify, cssMinify, replace404, favicon, headercacheDay, headercacheType) <> (ProxySetting.tupled, ProxySetting.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (settingId.?, projectId, contentEncoding, jsMinify, cssMinify, replace404, favicon, headercacheDay, headercacheType).shaped.<>({r=>import r._; _1.map(_=> ProxySetting.tupled((_1.get, _2, _3, _4, _5, _6, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column setting_id AutoInc, PrimaryKey */
  val settingId: Column[Int] = column[Int]("setting_id", O.AutoInc, O.PrimaryKey)
  /** Database column project_id  */
  val projectId: Column[Option[Int]] = column[Option[Int]]("project_id")
  /** Database column content_encoding  */
  val contentEncoding: Column[Option[String]] = column[Option[String]]("content_encoding")
  /** Database column jsminify  */
  val jsMinify: Column[Option[Boolean]] = column[Option[Boolean]]("jsminify")
  /** Database column cssminify  */
  val cssMinify: Column[Option[Boolean]] = column[Option[Boolean]]("cssminify")
  /** Database column replace404  */
  val replace404: Column[Option[Boolean]] = column[Option[Boolean]]("replace404")
  /** Database column favicon  */
  val favicon: Column[Option[Int]] = column[Option[Int]]("favicon")
  /** Database column headercache_day  */
  val headercacheDay: Column[Option[String]] = column[Option[String]]("headercache_day")
  /** Database column headercache_type  */
  val headercacheType: Column[Option[String]] = column[Option[String]]("headercache_type")
}

