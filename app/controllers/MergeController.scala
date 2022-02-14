package controllers

import models._
import com.feedle.feo.optimize.merger.Merger
import com.feedle.feo.util.OptimizeType
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

// 한번에 하나 !
object MergeController extends Controller {
  def create_merge(req_project_id:Int) = Action {implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")

    val listStr = rs.body.asFormUrlEncoded.get("list")(0)
    val json = Json.parse(listStr)
    val item = (json \\ "objectId").seq.map {x=> x.as[Int]}

    val wc = DBController.getWebContent(item)
    val merger = new Merger(Seq(wc))
    val result = merger.origin_dest.head
    val size = merger.size

    import play.api.db.slick.Config.driver.simple._
    import play.api.Play.current
    import play.api.db.slick.DB

    DB.withSession { implicit session =>
      lazy val webcontents = WebContents.tQuery
      lazy val optimizes = OptimizeRules.tQuery
      lazy val merges = MergeRules.tQuery

      //update webcontent size
      webcontents.where(_.objectId === result._2.objectId).update(result._2.setSize(size))

      val optObj : OptimizeRule =
        new OptimizeRule(0, req_project_id, OptimizeType.Merge, new java.sql.Timestamp(System.currentTimeMillis),Some(result._2.objectId) )

      val optId : Int = optimizes returning optimizes.map(_.optimizeId) += optObj

      var i = -1
      def increaseI : Int = {i = i + 1;  i}
      val mergObjs : Seq[MergeRule] = result._1.map { x=>
        new MergeRule(x.objectId, optId, increaseI)
      }

      merges ++= mergObjs
    }

    Ok("")
  }
}
