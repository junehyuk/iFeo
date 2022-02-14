package models


import java.io.{File, FileInputStream}
import java.net.URL
import java.util.{Collection => JCollection, Iterator => JIterator, List => JList, Map => JMap}

import com.feedle.feo.util.ContentType
import com.feedle.feo.util.ContentType.ContentType
import com.ning.http.client.FluentCaseInsensitiveStringsMap
import play.api.db.slick.Config.driver.simple._

import scala.collection.JavaConversions._
import scala.collection.convert.wrapAsJava
import scala.collection.{JavaConversions, mutable}
import scala.io.Source
import scala.slick.jdbc.{GetResult => GR}


class RequestHeader() extends mutable.HashMap[String, Iterable[String]] with HttpHeader {

  def +(str: String):RequestHeader = this ++= toScalaHeaderMap(str)
  def +(map:Map[String,Iterable[String]]):RequestHeader = this ++= map
  def +(kv:(String,String)*):RequestHeader = this ++= kv.map{x=> x._1->x._2.split(',').toSeq}

  def ++(map:mutable.Map[String,Iterable[String]]):RequestHeader = this ++= map
  override def toString = {
    this.map{x=>x._1 + "=@@@@@=" + x._2.mkString(";;;")}.mkString("&&&")
  }
}
class ResponseHeader extends mutable.HashMap[String, Iterable[String]] with HttpHeader {

  def +(str: String):ResponseHeader = this ++= toScalaHeaderMap(str)
  def +(map:Map[String,Iterable[String]]):ResponseHeader = this ++= map
  def +(kv:(String,String)*):ResponseHeader = this ++= kv.map{x=> x._1->x._2.split(',').toSeq}
  def +(fluentHeader : FluentCaseInsensitiveStringsMap) : ResponseHeader = this ++= toChickens(fluentHeader)

  def ++(map:mutable.Map[String,Iterable[String]]):ResponseHeader = this ++= map

  def getContentType = this.get("Content-Type").getOrElse(Seq("*/*")).head
  def getContentTypeNum : Int = ContentType.getNumFromContentType(getContentType)
  override def toString = {
    this.map{x=>x._1 + "=@@@@@=" + x._2.mkString(";;;")}.mkString("&&&")
  }
}

trait HttpHeader {

  def toChickens(from: FluentCaseInsensitiveStringsMap):Map[String, Iterable[String]]={
    from.mapValues(_.toIterable).toMap
  }

  //   def toScalaHeaderMap(fluentHeader : JMap[String, JCollection[String]]) : mutable.HashMap[String, Iterable[String]] = {
  //    mutable.HashMap(fluentHeader.mapValues(_.toIterable).toSeq:_*)
  //  }
  def toJavaHeaderMap(headerMap : mutable.HashMap[String, Iterable[String]]) : JMap[String, JCollection[String]] = {
    JavaConversions.mapAsJavaMap (headerMap.map{x =>
      x._1 -> wrapAsJava.asJavaCollection(x._2)
    })
  }
  def toScalaHeaderMap(headerStr : String) : mutable.HashMap[String, Iterable[String]] = {
    mutable.HashMap(headerStr.split("&&&").map{_.split("=@@@@@=")}.filter{_.size>1}.map{headers=>
      headers(0) -> headers(1).split(";;;").toIterable }.toSeq :_*)
  }
  def toJavaHeaderMap(headerStr : String) : JMap[String, JCollection[String]] = {
    JavaConversions.mapAsJavaMap (headerStr.split(';').map{x =>
      val headers = x.split('=')
      headers(0) -> wrapAsJava.asJavaCollection( headers(1).split(',').toIterable )}.toMap )
  }
}

object HttpHeader extends HttpHeader{
  val defaultRequest  = Map(("User-Agent", "Mozilla/5.0"),
    ("Content-Type", "text/html"),
    ("Accept", "*/*"),
    ("Accept-Encoding", "gzip, deflate, sdch"))
  val defaultRequestHeader : RequestHeader = new RequestHeader + defaultRequest.map{x=> x._1 -> x._2.split(',').toSeq}
}

/** Entity class storing rows of table WebContents
  *  @param objectId Database column object_id AutoInc, PrimaryKey
  *  @param projectId Database column project_id
  *  @param originUrlStr Database column origin_url
  *  @param localPath Database column local_path
  *  @param contentTypeValue Database column content_type
  *  @param request Database column request
  *  @param response Database column response
  *  @param isgen Database column isgen
  *  @param iscdn Database column iscdn  */
case class WebContent(objectId: Int, projectId: Int, originUrlStr: String, localPath: String,
                      contentTypeValue: Int, size : Option[Long], request: Option[String], response: Option[String],
                      isgen: Boolean, iscdn: Boolean) {
  //val localfileName : String = Util.getFileName(newLocalPath)     // todo : delete this

  def body = Source.fromFile(localPath,"UTF-8")
  def bodyInputStream = new FileInputStream(localPath)
  val contentType : ContentType = ContentType.getMalformedTypeFromNum(contentTypeValue)
  lazy val originUrl : URL = new URL(originUrlStr)
  lazy val localFile : File = new File(localPath)
  lazy val localFileName : String = { val filename = localFile.getName; if("".equals(filename)) "index.html" else filename }

  val localPathWithoutQuery = {
    val at = localPath.indexOf('@')
    localPath.substring(0, if(at == -1) localPath.length-1 else at)
  }

  val requestHeader = if (request.isDefined) new RequestHeader + request.get else new RequestHeader()
  val responseHeader = if (response.isDefined) new ResponseHeader + response.get else new ResponseHeader()

//  implicit def String2HashSet : mutable.HashSet[String] = {
//    mutable.HashSet[String](redirectUrl.split('\n'):_*)
//  }
//  implicit def HashSet2String : String = {
//    redirectUrls.mkString("\n")
//  }

  //lenght = .뒤에 가지고오고싶은 길이 쿼리 삭제해서 가져옴
  def stringAfterDot(length : Int) : String = {
    val dot = localPathWithoutQuery.lastIndexOf('.')
    localPathWithoutQuery.substring(dot+1, dot+1+length).toLowerCase
  }
  def setObjectId(oid : Int ) : WebContent= {
    new WebContent(oid, this.projectId, this.originUrlStr, this.localPath, this.contentTypeValue, this.size, this.request, this.response, this.isgen, this.iscdn)
  }
  def setSize(size : Long ) : WebContent= {
    new WebContent(this.objectId, this.projectId, this.originUrlStr, this.localPath, this.contentTypeValue, Some(size), this.request, this.response, this.isgen, this.iscdn)
  }
  override def toString : String = {
    "\nobjectId : " + objectId +
      "\nprojectId : " + projectId +
      "\noriginUrl : " + originUrlStr +
      "\nlocalPath : " + localPath +
      "\ncontentType : " + contentTypeValue +
      "\nrequest : " + request +
      "\nresponse : " + response +
      "\nisgen : " + isgen +
      "\niscdn : " + iscdn + "\n"

  }
}

object WebContents{
  implicit def GetResultWebContent(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]], e3: GR[Boolean]): GR[WebContent] = GR{
    prs => import prs._
      WebContent.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[Int], <<?[Long], <<?[String], <<?[String], <<[Boolean], <<[Boolean]))
  }

  /** Collection-like TableQuery object for table WebObjects */
  lazy val tQuery = new TableQuery(tag => new WebContents(tag))

}
/** Table description of table web_objects. Objects of this class serve as prototypes for rows in queries. */
class WebContents(tag: Tag) extends Table[WebContent](tag, "web_contents") {
  def * = (objectId, projectId, originUrlStr, localPath, contentType, size, request, response, isgen, iscdn) <> (WebContent.tupled, WebContent.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (objectId.?, projectId.?, originUrlStr.?, localPath.?, contentType.?, size, request, response, isgen.?, iscdn.?).shaped.<>({r=>import r._; _1.map(_=> WebContent.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7, _8, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column object_id AutoInc, PrimaryKey */
  val objectId: Column[Int] = column[Int]("object_id", O.AutoInc, O.PrimaryKey)
  /** Database column project_id  */
  val projectId: Column[Int] = column[Int]("project_id")
  /** Database column origin_url  */
  val originUrlStr: Column[String] = column[String]("origin_url")
  /** Database column local_path  */
  val localPath: Column[String] = column[String]("local_path")
  /** Database column content_type  */
  val contentType: Column[Int] = column[Int]("content_type")
  /** Database column size  */
  val size: Column[Option[Long]] = column[Option[Long]]("size")
  /** Database column request  */
  val request: Column[Option[String]] = column[Option[String]]("request")
  /** Database column response  */
  val response: Column[Option[String]] = column[Option[String]]("response")
  /** Database column isgen  */
  val isgen: Column[Boolean] = column[Boolean]("isgen")
  /** Database column iscdn  */
  val iscdn: Column[Boolean] = column[Boolean]("iscdn")

  //
  //  /** Foreign key referencing Projects (database name project_id) */
  //  lazy val projectsFk = foreignKey("project_id", pid, Projects.tQuery)(r => r.pid, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
}
