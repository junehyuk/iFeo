package controllers

import play.api.mvc.{Session=>pSession}
import models._
import play.api.db.slick._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._

/**
 * Created by infinitu on 2014. 4. 24..
 */
object SessionManager {
  def checkPagePermission(session: pSession, page: Page):Boolean = true

  lazy val users = Users.Users
  lazy val projects = Projects.tQuery
  lazy val pages = Pages.tQuery


  def checkUser(session:pSession):Boolean = {
    if(session.get("user") != None) true
    else false
  }

  def getUserId(id:String):Int = DB.withSession{implicit dbSession=>
    val owner = users.where(_.id === id).map(p=>p.userId).firstOption
    return owner.get
  }

  def checkProject(session:pSession,projectId:Int):Boolean=DB.withSession{implicit dbSession=>
    if(checkUser(session)){
      val owner = projects.where(_.projectId===projectId).map(p=>p.userId).firstOption
      if(owner.isEmpty) return false
      if(owner.get == getUserId(session.get("user").get)) return true
      else return false
    } else {
      return false
    }
  }

}
