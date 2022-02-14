package controllers

import models._
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
* Created by infinitu on 2014. 4. 24..
*/
object PageController extends Controller{



  def refreshPage(reqPageID:Int) = Action.async{implicit rs=>
    val projectAndPage:(Int,String,Page) = DB.withSession{implicit session=>
      (for{
        (page,project) <- Pages.tQuery.filter(_.pageId === reqPageID) innerJoin Projects.tQuery on (_.projectId === _.projectId)
      } yield(project.projectId,project.domain,page)).first()
    }

    if(!SessionManager.checkProject(session,projectAndPage._1)){
      Future{
        Forbidden("does not have permission.")
      }
    }
    else{
      PageHtmlManager.refreshPage(projectAndPage._3,projectAndPage._2)
      PageHtmlManager.withHtmlObj(projectAndPage._3.pageId)(x=>Ok(x.get.domTree.toString))
    }
  }

  /**
   * 하드에 저장된 변경되기 전 의 페이지의 HTML코드
   * @param reqPageId 페이지의 ID
   * @return 못찾았을시 404 not found, 찾으면, 200 Ok 와 함께 Page의 HTML코드
   */
  def getOriginPageCode(reqPageId:Int)=Action.async{implicit rs=>
    val page = DBController.getPage(reqPageId)
    if(!SessionManager.checkPagePermission(rs.session,page)){
      Future{
        Forbidden("does not have permission.")
      }
    }
    else{
      PageHtmlManager.withHtmlObj(reqPageId){
        case Some(html)=>
          Ok(Jsoup.parse(html.webContent.body.mkString).toString)
        case None=>
          NotFound("No Such Page")
      }
    }
  }

  /**
   * 메모리에 활성화된 변경 된 페이지의 HTML코드
   * @param reqPageId 페이지의 ID
   * @return 못찾았을시 404 not found, 찾으면, 200 Ok 와 함께 Page의 HTML코드
   */
  def getChangedPageCode(reqPageId:Int)=Action.async { implicit rs =>
    val page = DBController.getPage(reqPageId)
    if (!SessionManager.checkPagePermission(rs.session, page)) {
      Future {
        Forbidden("does not have permission.")
      }
    }
    else {
      PageHtmlManager.withHtmlObj(reqPageId) {
        case Some(html) =>
          Ok(html.domTree.toString)
        case None =>
          NotFound("No Such Page")
      }
    }
  }

  def isPageBusy(reqPageId:Int)=Action{ implicit  rs=>
    val page = DBController.getPage(reqPageId)
    if (!SessionManager.checkPagePermission(rs.session, page)) {
      Forbidden("does not have permission.")
    }
    else {
      Ok(PageHtmlManager.isIdle(reqPageId).toString)
    }
  }

  def waitPageLoaded(reqPageId:Int)=Action.async{implicit  rs=>
    val page = DBController.getPage(reqPageId)
    if (!SessionManager.checkPagePermission(rs.session, page)) {
      Future {
        Forbidden("does not have permission.")
      }
    }
    else {
      PageHtmlManager.withHtmlObj(reqPageId) {
        case Some(html) =>
          Ok("ok")
        case None =>
          NotFound("No Such Page")
      }
    }
  }
}
