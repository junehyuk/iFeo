package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table ProxyTrafficCounts
  *  @param projectId Database column project_id
  *  @param proxyId Database column proxy_id
  *  @param datetime Database column datetime
  *  @param traffic Database column traffic  */
case class ProxyTrafficCount(projectId: Int, proxyId: Int, datetime: java.sql.Timestamp, traffic: Long)
object ProxyTrafficCounts {
  /** GetResult implicit for fetching ProxyTrafficCountsRow objects using plain SQL queries */
  implicit def GetResultProxyTrafficCount(implicit e0: GR[Int], e1: GR[java.sql.Timestamp], e2: GR[Long]): GR[ProxyTrafficCount] = GR{
    prs => import prs._
      ProxyTrafficCount.tupled((<<[Int], <<[Int], <<[java.sql.Timestamp], <<[Long]))
  }

  /** Collection-like TableQuery object for table ProxyTrafficCounts */
  lazy val tQuery = new TableQuery(tag => new ProxyTrafficCounts(tag))
}
/** Table description of table proxy_traffic_counts. Objects of this class serve as prototypes for rows in queries. */
class ProxyTrafficCounts(tag: Tag) extends Table[ProxyTrafficCount](tag, "proxy_traffic_counts") {
  def * = (projectId, proxyId, datetime, traffic) <> (ProxyTrafficCount.tupled, ProxyTrafficCount.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (projectId.?, proxyId.?, datetime.?, traffic.?).shaped.<>({r=>import r._; _1.map(_=> ProxyTrafficCount.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column project_id  */
  val projectId: Column[Int] = column[Int]("project_id")
  /** Database column proxy_id  */
  val proxyId: Column[Int] = column[Int]("proxy_id")
  /** Database column datetime  */
  val datetime: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("datetime")
  /** Database column traffic  */
  val traffic: Column[Long] = column[Long]("traffic")
}
