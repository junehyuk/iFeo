package controllers

import controllers.CssSpriteController._
import controllers.ProxyDeployController._
import models.ProxyFileSettings._
import models._
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.libs.json.{JsObject, JsNumber, Json, JsArray}
import play.api.mvc.{Action, Controller}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Created by infinitu on 2014. 6. 20..
 */
object ProxyFileSettingController extends Controller {

  def getAutoProxySettingsInfo(req_project_id: Int) = Action { implicit rs =>

    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")

    val ReplaceMap = DB.withSession { implicit session =>
      (ReplaceRules.tQuery.where(_.projectId === req_project_id) innerJoin WebContents.tQuery on (_.destObject === _.objectId)).list().map { x =>
        x._1.fromObject -> x._2
      }.toMap
    }

    val AutoProxyMap = mutable.HashMap[Int, (String, mutable.Buffer[Int])]()

    ReplaceMap.map { x =>
      def getFin(xx: WebContent): WebContent = {
        if (ReplaceMap.get(xx.objectId).isDefined)
          getFin(ReplaceMap.get(xx.objectId).get)
        else
          xx
      }
      val finDest = getFin(x._2)
      val seq = AutoProxyMap get finDest.objectId
      seq.getOrElse {
        val newNode = finDest.originUrl.toString -> mutable.Buffer[Int]()
        AutoProxyMap += finDest.objectId -> newNode
        newNode
      }._2 += x._1
    }

    Ok(JsArray(AutoProxyMap.map(x =>
      Json.obj(
        "objectId" -> x._1,
        "url" -> x._2._1,
        "fromArray" -> JsArray(x._2._2.map(JsNumber(_)).toSeq)
      )).toSeq)
    )
  }

  def getAutoProxySettings(req_project_id: Int) = Action { implicit rs =>

    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")

    val settings = DB.withSession { implicit session =>
      tQuery.where(_.projectId === req_project_id).list()
    }
    Ok(JsArray(settings.filter(_.autoProxy.isDefined).map(x =>
      Json.obj(
        "objectId" -> x.objectId,
        "option" -> x.autoProxy.get
      )).toSeq)
    )
  }



  def getCacheControlSettings(req_project_id: Int) = Action { implicit rs =>

    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")

    val settings = DB.withSession { implicit session =>
      tQuery.where(_.projectId === req_project_id).list()
    }
    Ok(JsArray(settings.filter(_.cacheControl.isDefined).map(x =>
      Json.obj(
        "objectId" -> x.objectId,
        "option" -> x.cacheControl.get
      )).toSeq)
    )
  }


  def getProxyCacheSettings(req_project_id: Int) = Action { implicit rs =>

    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")

    val settings = DB.withSession { implicit session =>
      tQuery.where(_.projectId === req_project_id).list()
    }
    Ok(JsArray(settings.filter(x=>x.proxyCache.isDefined && x.proxyCache.get).map(x =>
      Json.obj(
        "objectId" -> x.objectId,
        "option" -> x.proxyCache.get
      )).toSeq)
    )
  }


  def setAutoProxySettings(req_project_id: Int) = Action { implicit rs =>
    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")
    else {
      val imgListStr = rs.body.asFormUrlEncoded.get("list")(0)
      val settingSeq = Json.parse(imgListStr).as[JsArray].value
      settingSeq.foreach { x =>
        val dest = (x \ "url").as[String]
        val from = (x \ "fromArray").as[JsArray].value.map(_.as[Int])
        val remove = (x\"remove").asOpt[Boolean]
        if(remove.isEmpty || !remove.get) {
          DB.withSession { implicit session =>
            val already = tQuery.where(_.objectId inSet from).map(_.objectId).list()
            from.filter(already.contains(_)).foreach(x => tQuery.where(_.objectId === x).map(_.autoProxy).update(Some(dest)))
            tQuery ++= from.filter(!already.contains(_)).map(new ProxyFileSetting(_, req_project_id, None, None, None, Some(dest)))
          }
        } else {
          DB.withSession { implicit session =>
            val already = tQuery.where(_.objectId inSet from).map(_.objectId).list()
            from.filter(already.contains(_)).foreach(x => tQuery.where(_.objectId === x).map(_.autoProxy).update(None))
          }
        }
      }

      Ok("ok")
    }
  }


  def setCacheControlSettings(req_project_id: Int) = Action { implicit rs =>
    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")
    else {
      val imgListStr = rs.body.asFormUrlEncoded.get("list")(0)
      val settingSeq = Json.parse(imgListStr).as[JsArray].value.map{ x =>
        (x \ "objectId").as[Int]->(x \ "option").asOpt[String]
      }

      DB.withSession { implicit session =>
        val already = tQuery.where(_.objectId inSet settingSeq.map(_._1)).map(_.objectId).list()
        settingSeq.filter(x=>already.contains(x._1)).foreach(x => tQuery.where(_.objectId === x._1).map(_.cacheControl).update(x._2))
        tQuery ++= settingSeq.filter(x=> !already.contains(x._1)).map(x=>new ProxyFileSetting(x._1, req_project_id,x._2, None, None, None))
      }

      Ok("ok")
    }
  }


  def setProxyCacheSettings(req_project_id: Int) = Action { implicit rs =>
    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")
    else {
      val imgListStr = rs.body.asFormUrlEncoded.get("list")(0)
      val settingSeq = Json.parse(imgListStr).as[JsArray].value.map{ x =>
        (x \ "objectId").as[Int]->(x \ "option").asOpt[Boolean]
      }

      DB.withSession { implicit session =>
        val already = tQuery.where(_.objectId inSet settingSeq.map(_._1)).map(_.objectId).list()
        settingSeq.filter(x=>already.contains(x._1)).foreach(x => tQuery.where(_.objectId === x._1).map(_.proxyCache).update(x._2))
        tQuery ++= settingSeq.filter(x => !already.contains(x._1)).map(x=>new ProxyFileSetting(x._1, req_project_id, None, Some(123456789),x._2, None))
      }

      Ok("ok")
    }
  }

}