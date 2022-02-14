package controllers

import java.sql.Timestamp

import models.ProxyTrafficCount
import play.api.mvc.Controller
import play.api.libs.json._
import models._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{DB, Session}

/**
 * Created by infinitu on 2014. 6. 18..
 */
object TrafficController extends Controller {

  def addTraffic(jsonStr:String,proxyId:Int) = {
    val json = Json.parse(jsonStr).as[JsObject].value
    val hosts = json.map{x=>x._1}.toSeq

    val projs = DB withSession { implicit  session=>
      Projects.tQuery.where(_.domain inSet hosts).list()
    }

    val newRows = projs.map{ proj=>
      val traffic = json get proj.domain
      val amount = (traffic.get \ "client" \ "amount").as[Long] + (traffic.get \ "origin" \ "amount").as[Long]
      new ProxyTrafficCount(proj.projectId,proxyId, new Timestamp(System.currentTimeMillis()),amount)
    }

    DB withSession{implicit session=>
      ProxyTrafficCounts.tQuery ++= newRows
    }

  }

}
