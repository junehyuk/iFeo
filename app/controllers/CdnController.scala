package controllers

import com.feedle.feo.optimize.ftpuploader.{FTPMapper, FtpUploader}
import com.feedle.feo.util._
import models._
import play.api.libs.json._
import play.api.mvc._


object CdnController extends Controller{

  def cdnupload(req_project_id:Int) = Action {implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")

    val listStr = rs.body.asFormUrlEncoded.get("list")(0)
    val json = Json.parse(listStr)
    val item   = (json \\ "objectId").seq.map {x=> x.as[Int]}

    val ftp = new FtpUploader()
    val wc = DBController.getWebContent(item)
    val origin_map : Seq[FTPMapper] = wc.map{x=>new FTPMapper(req_project_id, x)}

    val map = origin_map.filter{x=>ftp.downloadAndUpload(x)}

    import play.api.Play.current
    import play.api.db.slick.Config.driver.simple._
    import play.api.db.slick.DB

    DB.withSession { implicit session =>

      lazy val optimizes = OptimizeRules.tQuery
      lazy val replaces = ReplaceRules.tQuery
      lazy val webcontents = WebContents.tQuery

      //optimize 생성, 등록
      val optObj : OptimizeRule = new OptimizeRule(0, req_project_id, OptimizeType.Cdn, new java.sql.Timestamp(System.currentTimeMillis), None)
      val optId : Int = optimizes returning optimizes.map(_.optimizeId) += optObj

      //webcontent생성, 등록
      val wcs : Seq[WebContent] =
        map.map{x=>
          new WebContent(0, req_project_id, x.cdnUrl, x.localPath, x.x.contentTypeValue, x.x.size, None, None, false, true) }
      val oIds = webcontents returning webcontents.map(_.objectId) ++= wcs
      val origin_dest : Seq[(WebContent, WebContent)]= {
        val q = for {
          w <- WebContents.tQuery if w.objectId inSet oIds
        } yield w
        q.list
      } zip wc

      //replace 생성, 등록
      val repObj : Seq[ReplaceRule] = origin_dest.map { x=>
        new ReplaceRule(0, req_project_id, optId, x._1.objectId, x._2.objectId)
      }
      replaces ++= repObj
    }

    Ok("")
  }
}
