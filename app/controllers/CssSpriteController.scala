package controllers


import com.feedle.feo.optimize.csssprite.{CssSprite, Margin}
import com.feedle.feo.weboject.HtmlObject
import models._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{Session, _}
import play.api.mvc.{Action, Controller}
import scala.collection.mutable
import play.api.libs.json._
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import java.sql.Timestamp
import com.feedle.feo.util.{OptimizeType, PathManager}
import java.io.File
import com.feedle.feo.content.ObjectManager
import scala.util.parsing.json.JSONArray

object CssSpriteController extends Controller{
  implicit def int2JsValue(n:Int):JsNumber = JsNumber(n)
  implicit def str2JsValue(str:String):JsString = JsString(str)

  lazy val pages = Pages.tQuery
  lazy val webObjects = WebContents.tQuery

  def spritePresetInformation(req_project_id:Int)=DBAction{implicit rs=>

    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")
    else{
      val pagelist = pages.filter(_.projectId===req_project_id).map(_.pageId).list
      val SpritableImages = new mutable.HashMap[Int,Margin] with mutable.SynchronizedMap[Int,Margin]
      import scala.concurrent.Await
      import scala.concurrent.duration._
      pagelist//.par
        .map{pageId=>
          val fu = PageHtmlManager.withHtmlObj[Option[HtmlObject]](pageId){x=>x}
          Await.result(fu,15 seconds)
        }
        .filter(_.isDefined)
        .foreach(_.get.SpritableImages.foreach({x=>
          val e=SpritableImages.get(x._1)
          if(e.isEmpty){
            SpritableImages+=x._1->x._2.margin
          }
          else
          {
            import scala.math.max
            val margin1 = x._2.margin
            val margin2 = e.get
            val newMargin = new Margin(
              max(margin1.top,margin2.top),
              max(margin1.right,margin2.right),
              max(margin1.left,margin2.left),
              max(margin1.bottom,margin2.bottom),
              margin1.width,margin1.height)
            SpritableImages+=x._1->newMargin
          }
        }))

      Ok( JsArray(SpritableImages.map(x=>
        Json.obj(
          "objectId" -> x._1,
          "width" -> x._2.width,
          "height" -> x._2.height,
          "top" -> x._2.top ,
          "bottom" -> x._2.bottom,
          "left" -> x._2.left,
          "right" -> x._2.right
        )).toSeq)
      )
    }
  }

  def create_sprite(req_project_id:Int) = Action {implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")
    else {
      val imgListStr = rs.body.asFormUrlEncoded.get("list")(0)
      val imgSeq = Json.parse(imgListStr).as[JsObject].value
      val imgList = imgSeq.map {
        x =>
          val obj = x._2.as[JsObject]
          val margin = new Margin(
            (obj \ "top").as[Int],
            (obj \ "right").as[Int],
            (obj \ "left").as[Int],
            (obj \ "bottom").as[Int],
            (obj \ "width").as[Int],
            (obj \ "height").as[Int]
          )
          val objid: Int = x._1.toInt //new URL((obj \ "obj_id").as[String])
          objid -> margin

      }.toList

      val targetObj = ObjectManager(PathManager.createSpriteName(), req_project_id).get
      val genFile = new File(targetObj.localPath)

      val imgPositionMap = CssSprite.createImage(genFile, imgList)
      val byteSize = genFile.length()

      DB.withSession { implicit session:Session =>
        val wctQ = WebContents.tQuery
        val optQ = OptimizeRules.tQuery
        val cssQ = CssSpriteRules.tQuery

        wctQ.where(_.objectId === targetObj.objectId).update(targetObj.setSize(byteSize))

        val optId = (optQ returning optQ.map(_.optimizeId)) +=
          new OptimizeRule(0, req_project_id, OptimizeType.CssSprite , new Timestamp(System.currentTimeMillis), Some(targetObj.objectId))
        cssQ ++= imgPositionMap.map {
          pos =>
            new CssSpriteRule(pos._1._1.objectId, optId, pos._2.x, pos._2.y, pos._1._2.width, pos._1._2.height)
        }
      }


      Ok.sendFile(
        content = genFile,
        inline = true
      )
    }
  }
}
