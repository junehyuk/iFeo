package controllers

import models._
import com.feedle.feo.optimize.imagecompressor.{JpgCompressor, Pngtastic}
import com.feedle.feo.util.OptimizeType
import play.api.libs.json._
import play.api.mvc._

object ImageController extends Controller {

  def create_optimize(req_project_id:Int) = Action {implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")

    val imageListStr = rs.body.asFormUrlEncoded.get("list")(0)

    //이렇게 test를 했는데 잘됐엇슴미다
    // compressionLevel where 0 is no compression and 9 is the highest available compression
    //    """{"compressLevel" : 5,"item":[{"object_id":12831},{"object_id":12832},{"object_id":12834}]}"""
    val listStr = rs.body.asFormUrlEncoded.get("list")(0)
    val json = Json.parse(listStr)
    val compressLevel = (json \ "compressLevel").as[Int]
    val item = ( json \\ "object_id" ).seq.map {x=> x.as[Int]}


    val wc : Seq[(String, WebContent)]= DBController.getWebContent(item).map{x=> x.stringAfterDot(2)->x}

    //      class JpgCompressor(val pid : Int, val files : Seq[File], val compressionLevel : Int = 5)
    val jpgImgs : Seq[WebContent] = wc.filter{ x => "jp".equals(x._1) }.map{x => x._2}
    val pngImgs : Seq[WebContent] = wc.filter{ x => "pn".equals(x._1) }.map{x => x._2}

    val jpgResultMap : Seq[(WebContent, WebContent)] = new JpgCompressor(req_project_id, jpgImgs, compressLevel).origin_dest
    val pngResultMap : Seq[(WebContent, WebContent)] = new Pngtastic(req_project_id, pngImgs, compressLevel).origin_dest

    import play.api.db.slick.Config.driver.simple._
    import play.api.Play.current
    import play.api.db.slick.DB

    DB.withSession { implicit session =>
      lazy val optimizes = OptimizeRules.tQuery
      lazy val replaces = ReplaceRules.tQuery

      val optObj : OptimizeRule =
        new OptimizeRule(0, req_project_id, OptimizeType.Image, new java.sql.Timestamp(System.currentTimeMillis), None)

      val optId : Int = optimizes returning optimizes.map(_.optimizeId) += optObj

      val repObjs : Seq[ReplaceRule]= (jpgResultMap ++: pngResultMap).map{x =>
        new ReplaceRule(0, req_project_id, optId, x._1.objectId, x._2.objectId)
      }

      replaces ++= repObjs
    }

    Ok("done")
  }
}