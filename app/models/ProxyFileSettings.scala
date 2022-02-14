package models

import play.api.db.slick.Config.driver.simple._

import scala.slick.jdbc.{GetResult => GR}


/** Entity class storing rows of table ProxyFileSettings
  *  @param objectId Database column object_id PrimaryKey
  *  @param projectId Database column project_id
  *  @param cacheControl Database column cache_control
  *  @param refreshCycle Database column refresh_cycle
  *  @param proxyCache Database column proxy_cache
  *  @param autoProxy Database column auto_proxy  */
case class ProxyFileSetting(objectId: Int, projectId: Int, cacheControl: Option[String], refreshCycle: Option[Int], proxyCache: Option[Boolean], autoProxy: Option[String])
object ProxyFileSettings {
  /** GetResult implicit for fetching ProxyFileSettingsRow objects using plain SQL queries */
  implicit def GetResultProxyFileSetting(implicit e0: GR[Int], e1: GR[Option[String]], e2: GR[Option[Int]], e3: GR[Option[Boolean]]): GR[ProxyFileSetting] = GR {
    prs => import prs._
      ProxyFileSetting.tupled((<<[Int], <<[Int], <<?[String], <<?[Int], <<?[Boolean], <<?[String]))
  }
  /** Collection-like TableQuery object for table ProxyFileSettings */
  lazy val tQuery = new TableQuery(tag => new ProxyFileSettings(tag))
}
/** Table description of table proxy_file_settings. Objects of this class serve as prototypes for rows in queries. */
class ProxyFileSettings(tag: Tag) extends Table[ProxyFileSetting](tag, "proxy_file_settings") {
  def * = (objectId, projectId, cacheControl, refreshCycle, proxyCache, autoProxy) <> (ProxyFileSetting.tupled, ProxyFileSetting.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (objectId.?, projectId.?, cacheControl, refreshCycle, proxyCache, autoProxy).shaped.<>({r=>import r._; _1.map(_=> ProxyFileSetting.tupled((_1.get, _2.get, _3, _4, _5, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column object_id PrimaryKey */
  val objectId: Column[Int] = column[Int]("object_id", O.PrimaryKey)
  /** Database column project_id  */
  val projectId: Column[Int] = column[Int]("project_id")
  /** Database column cache_control  */
  val cacheControl: Column[Option[String]] = column[Option[String]]("cache_control")
  /** Database column refresh_cycle  */
  val refreshCycle: Column[Option[Int]] = column[Option[Int]]("refresh_cycle")
  /** Database column proxy_cache  */
  val proxyCache: Column[Option[Boolean]] = column[Option[Boolean]]("proxy_cache")
  /** Database column auto_proxy  */
  val autoProxy: Column[Option[String]] = column[Option[String]]("auto_proxy")
}
