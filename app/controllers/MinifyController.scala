package controllers

import java.sql.Timestamp

import com.feedle.feo.optimize.minifier.Minifier
import com.feedle.feo.util.OptimizeType
import models._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, Controller}


object MinifyController extends Controller{

  def create_minify(req_project_id:Int) = Action {implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")

    val listStr = rs.body.asFormUrlEncoded.get("list")(0)
    val json = Json.parse(listStr)
    val item   = (json \\ "objectId").seq.map {x=> x.as[Int]}
    

    val wc = DBController.getWebContent(item)
    val result = new Minifier(wc).origin_dest.toSeq


    DB.withSession { implicit session =>
      lazy val webcontents = WebContents.tQuery
      lazy val optimizes = OptimizeRules.tQuery
      lazy val replaces = ReplaceRules.tQuery

      //update webcontent size
      //여기 file io너무 많음 ㅜㅜ흐규규
      result.map { x =>
        webcontents.where(_.objectId === x._2.objectId).update(x._2.setSize(x._2.localFile.length()))
      }

    //optimize 생성, 등록
      val optObj : OptimizeRule =
        new OptimizeRule(0, req_project_id, OptimizeType.Minify, new Timestamp(System.currentTimeMillis), None)

      val optId : Int = optimizes returning optimizes.map(_.optimizeId) += optObj

    //replace 생성, 등록
      val repObj : Seq[ReplaceRule] = result.map { x=>
        new ReplaceRule(0, req_project_id, optId, x._1.objectId, x._2.objectId)
      }

      replaces ++= repObj
    }

    Ok("")
  }
}
