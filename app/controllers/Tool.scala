package controllers

import com.feedle.feo.optimize.csssprite.CssSpriteInvoker
import com.feedle.feo.optimize.merger.MergeInvoker
import com.feedle.feo.optimize.replace.{ReplaceInvoker, Replace}
import com.feedle.feo.util.OptimizeType._
import controllers.DBController
import play.api.libs.json
import play.api.mvc._
import play.api.mvc.Action
import play.api.mvc.Session


import models._
import play.api.db.slick._
import play.api.Play.current
import play.api.mvc.Controller
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json.{JsError, JsSuccess, JsResult, Json,JsValue}
import play.api.mvc.Session
import play.api.db.slick.Session
import play.api.mvc.Session
import scala.io.Source
import com.feedle.feo.wpt.WPTConnector
import com.feedle.feo.util.{OptimizeType, PathManager}
import java.io.File
import scala.concurrent.Await
import play.api.libs.ws.WS
import play.Logger

import java.io.File
import scala.concurrent.duration._
import scala.concurrent.Await
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.Some
import java.nio.file.{StandardCopyOption, Paths, Files, Path}

import scala.util.parsing.json.JSONArray


object Tool extends Controller {

  val permissionErrorString:String = "권한 없는 프로젝트 접근을 시도하였습니다."


  // 프로젝트 목록
  def listProject(userid:Int)=DB.withSession{implicit session:play.api.db.slick.Session=>
    Projects.tQuery.filter(_.userId === userid).list()
  }

  // 프로젝트 도메인정보 가져오기
  def getProjectDomain(req_project_id:Int):String = {
    val myProject:Project = DBController.getProject(req_project_id)
    myProject.domain
  }


  def thumbnailImage(data:String):String = {
    val jsonData = Json.parse(data)
    return (jsonData \ "Seoul:Chrome").as[String]

  }

  def pageSpeedLocal(data:String):String = {
    val jsonData = Json.parse(data)
    return (jsonData \ "Singapore_IE8").as[String]

  }

  // 프로젝트 선택 뷰
  def index = Action { implicit rs =>
      if (SessionManager.checkUser(rs.session)){
        Ok(views.html.tool.index())
      } else {
        Redirect(routes.Member.login())
      }
  }

  // 최적화 관리툴 메인
  def main(req_project_id: Int, page: String) = {
    import views.html._
    Action { implicit request =>

      val magic = "{}";
      if(!SessionManager.checkProject(request.session,req_project_id))
        Forbidden(views.html.tool.error(permissionErrorString))
      else {
        val myProject:Project = DBController.getProject(req_project_id)
        val myProxy:ProxySetting = DBController.getProxy(req_project_id)

        val contents = page match {
          case "dashboard"=> Some(tool.dashboard(req_project_id,myProject))
          case "optimize" => Some(tool.optimize(req_project_id,myProject))
          case "pages" => Some(tool.pages(req_project_id))
          case "resource" => Some(tool.resource(req_project_id,magic))
          case "setting" => Some(tool.setting(req_project_id,myProject,myProxy,magic))
          case _      => None
        }
        if(contents.isDefined){
          val mainView = tool.main(req_project_id,page)(contents.get)
          Ok(views.html.frame(page,req_project_id)(mainView))
        } else {
          NotFound(tool.error("페이지를 찾을 수 없습니다."))
        }
      }
    }
  }

  def changeManagerSetting(req_project_id: Int) = {
    import views.html._
    Action { implicit request =>

      val magic:String = request.body.asFormUrlEncoded.get("applyRule")(0)
      if(!SessionManager.checkProject(request.session,req_project_id))
        Forbidden(views.html.tool.error(permissionErrorString))
      else {
        val myProject:Project = DBController.getProject(req_project_id)
        val myProxy:ProxySetting = DBController.getProxy(req_project_id)
        val contents = Some(tool.setting(req_project_id,myProject,myProxy,magic))
        val mainView = tool.main(req_project_id,"setting")(contents.get)
        Ok(views.html.frame("setting",req_project_id)(mainView))
      }
    }

  }

  def changeManagerResource(req_project_id: Int) = {
    import views.html._
    Action { implicit request =>

      val magic:String = request.body.asFormUrlEncoded.get("applyRule")(0)
      if(!SessionManager.checkProject(request.session,req_project_id))
        Forbidden(views.html.tool.error(permissionErrorString))
      else {
        val contents = Some(tool.resource(req_project_id,magic))
        val mainView = tool.main(req_project_id,"resource")(contents.get)
        Ok(views.html.frame("resource",req_project_id)(mainView))
      }
    }

  }



  //////////////////////////////////////////////////
  //프로젝트 설정
  //////////////////////////////////////////////////

  // 프로젝트 추가
  def addProject() = Action { implicit request =>

    val project_title = request.body.asFormUrlEncoded.get("title")(0)
    val project_domain = request.body.asFormUrlEncoded.get("domain")(0)
    val project_user:Int = SessionManager.getUserId( request.session.get("user").get )
    val project_proxyuse:Int = request.body.asFormUrlEncoded.get("proxyuse")(0).toInt
    val project_origin_ip = request.body.asFormUrlEncoded.get("origin_ip")(0)
    val project_test_id = WPTConnector(PathManager.deleteHTTP(project_domain)).toString
    val simulation_test_id = project_test_id.toString

    val projectInfo:Project = new Project(0, Some(project_title), project_user, project_domain, project_origin_ip, Some(project_proxyuse), Some(project_test_id), Some(simulation_test_id))
    val result:Int = DBController.addProject(projectInfo)

    val page_path = "/"
    val page_header = ""
    val page_query = ""
    val pageInfo: Page = new Page(0, result, page_path, Some(page_header), Some(page_query))
    DBController.addPage(pageInfo)

    //settingId.?, projectId, contentEncoding, jsMinify, cssMinify, replace404, favicon, headercacheDay, headercacheType
    val proxyInfo: ProxySetting = new ProxySetting(0, Some(result), Some(""), Some(false), Some(false), Some(false), Some(0), Some("259200"), Some("max-age"))
    DBController.addProxy(proxyInfo)

    Ok("Ok")
  }





  // 프로젝트 수정
  def saveProject(req_project_id: Int) = Action { implicit request =>

    val project_title:String = request.body.asFormUrlEncoded.get("title")(0)
    val project_proxyuse:Int = request.body.asFormUrlEncoded.get("proxyuse")(0).toInt
    val project_origin_ip = request.body.asFormUrlEncoded.get("origin_ip")(0)

    val projectInfo:Project = DBController.getProject(req_project_id) //new Project(req_project_id, Some(project_title), project_user, project_domain, project_origin_ip, Some(project_proxyuse), None)
    val newporjectInfo = new Project(req_project_id, Some(project_title), projectInfo.userId, projectInfo.domain, project_origin_ip, Some(project_proxyuse), projectInfo.wptTestId, projectInfo.simulTestId)

    DBController.modifyProject(newporjectInfo)

    val contentEncoding:String = request.body.asFormUrlEncoded.get("contentEncoding")(0) //"gzip"
    val jsMinify:Boolean = if(request.body.asFormUrlEncoded.get("jsMinify")(0).toInt==1) true else false //false
    val cssMinify:Boolean = if(request.body.asFormUrlEncoded.get("cssMinify")(0).toInt==1) true else false //false
    val replace404:Boolean = if(request.body.asFormUrlEncoded.get("replace404")(0).toInt==1) true else false //false
    val favicon:Int = request.body.asFormUrlEncoded.get("favicon")(0).toInt //false
    val headercacheType:String = "max-age"
    val headercacheDay:String = request.body.asFormUrlEncoded.get("headercacheDay")(0) //"0"

    //settingId.?, projectId, contentEncoding, jsMinify, cssMinify, replace404, favicon, headercacheDay, headercacheType
    val proxyInfo:ProxySetting = DBController.getProxy(req_project_id)
    val newproxyInfo = new ProxySetting(proxyInfo.settingId, Some(req_project_id), Some(contentEncoding), Some(jsMinify), Some(cssMinify), Some(replace404), Some(favicon), Some(headercacheDay), Some(headercacheType))

    DBController.modifyProxy(newproxyInfo)
    Ok("Ok")

  }


  // 프로젝트 삭제
  def removeProject(req_project_id: Int) = Action { implicit request =>

    if(SessionManager.checkProject(request.session,req_project_id)) {

      val user_id = request.session.get("user").get
      val user_pwd = request.body.asFormUrlEncoded.get("pwd")(0)

      println(user_id)
      println(user_pwd)

      val result = DBController.login(user_id,user_pwd)
      val loginResult: JsResult[String] = (result \ "result").validate[String]

      loginResult match {
        case s: JsSuccess[String] => {
          DBController.removeProject(req_project_id);
          Ok("Ok")
        }
        case e: JsError => {
          Ok("비밀번호가 틀렸습니다.")
        }
      }

    } else {
      Forbidden("permissionError")
    }

  }


  def purgeProject(req_project_id: Int) = Action { implicit rs =>
    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")
    else DB.withSession{implicit session=>
      WebContents.tQuery.where(_.projectId === req_project_id).delete
      OptimizeRules.tQuery.where(_.projectId === req_project_id).delete
     // MergeRules.tQuery.where(_.projectId === req_project_id).delete()
     // SpriteRules.tQuery.where(_.projectId === req_project_id).delete()
      ReplaceRules.tQuery.where(_.projectId === req_project_id).delete

      Ok("ok")
    }
  }


  ///////////////////////////////////////////////
  // 페이지
  ///////////////////////////////////////////////

  // 페이지 추가
  def addPage(req_project_id: Int) = Action { implicit request =>

    if(SessionManager.checkProject(request.session,req_project_id)) {

      val page_path = request.body.asFormUrlEncoded.get("path")(0)
      val page_header = request.body.asFormUrlEncoded.get("header")(0)
      val page_query = request.body.asFormUrlEncoded.get("query")(0)

      val pageInfo: Page = new Page(0, req_project_id, page_path, Some(page_header), Some(page_query))
      val result = DBController.addPage(pageInfo)
      Ok("Ok")

    } else {
      Forbidden("permissionError")
    }
  }


  // 페이지 삭제
  def removePage(req_project_id: Int) = Action { implicit request =>

    if(SessionManager.checkProject(request.session,req_project_id)) {

      val pageNo:Int = request.body.asFormUrlEncoded.get("pageId")(0).toInt
      DBController.removePage(pageNo)
      Ok("Ok")

    } else {
      Forbidden("permissionError")
    }

  }

  // 페이지 목록
  def listPage(req_project_id: Int) = Action { implicit request =>

    if(SessionManager.checkProject(request.session,req_project_id)) {
      Ok(DBController.listPage(req_project_id))
    } else {
      Forbidden("permissionError")
    }

  }

  def getAllWebContentWithCdn(req_project_id: Int) = Action { implicit request =>

    if(SessionManager.checkProject(request.session,req_project_id)) {
      Ok(DBController.getAllWebContentWithCdn(req_project_id))
    } else {
      Forbidden("permissionError")
    }

  }

  def getAllWebContent(req_project_id: Int) = Action { implicit request =>

    if(SessionManager.checkProject(request.session,req_project_id)) {
      Ok(DBController.getAllWebContent(req_project_id))
    } else {
      Forbidden("permissionError")
    }

  }

  def getPageSpeedJson(pageKey:String) = Action {
    import play.api.libs.ws.WS
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent._
    import scala.concurrent.duration._

    //Ok(Await.result(WS.url("https://www.googleapis.com/pagespeedonline/v1/runPagespeed?url=http://www.naver.com/&locale=ko&key=AIzaSyA7-kxaWdmDwxzhjxIxUX3Bn5Oxr8oHZ7U").get(), 10000 millis).body)
    Ok(Await.result(WS.url("http://www.webpagetest.org/result/"+pageKey+"/1_pagespeed.txt").get(), 10000 millis).body)
  }
  //https://www.googleapis.com/pagespeedonline/v1/runPagespeed?url=http://www.naver.com/&key=AIzaSyA7-kxaWdmDwxzhjxIxUX3Bn5Oxr8oHZ7U

  def getWebpagetestXML(pageKey:String) = Action {
    import play.api.libs.ws.WS
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent._
    import scala.concurrent.duration._

    Ok(Await.result(WS.url("http://www.webpagetest.org/xmlResult/"+pageKey+"/").get(), 3000 millis).body)
  }



  ///////////////////////////////////////////////
  // PageDiff 확인
  ///////////////////////////////////////////////

  // 최적화 후 페이지
  def pageAfter(req_project_id: Int, pId: Int) = Action { implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")
    else {
      Ok(views.html.index("Hello"))
    }
  }


  // 최적화 전 페이지
  def pageBefore(req_project_id: Int, pId: Int) = Action { implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")
    else {
      Ok(views.html.index("Hi"))
    }
  }








  //140617_D9_K7V

// 비디오 다운로드
  def downloadVideo (testId : String)= {

     // val downloadUrl_F = "http://webpagetest.org/video/download.php?id="+testId  //+".1.0"
      val createVideoUrl_F = "http://www.webpagetest.org/video/create.php?tests="+testId //+"-r:1-c:1&id="+testId+".1.0"

      val rr = Await.result(WS.url(createVideoUrl_F).get(), 10000 millis)
     // Await.result(WS.url(createVideoUrl_R).get(), 10000 millis)
     val reUrl = rr.getAHCResponse.getResponseBody;
     val downloadUrl_F = "http://webpagetest.org"+"""/video/download\.php\?id=([^"]+)""".r.findFirstIn(reUrl).get

      def isVideo(url : String, testId:String) = {
        val newfilename = new File(System.getProperty("user.home") + "/tmp/0/video/" + testId + ".mp4")
        newfilename.getParentFile.mkdirs()

        val result = Await.result(WS.url(url).get(), 5000 millis)
        val contentType = result.getAHCResponse.getHeaders("Content-Type")
        if (contentType.get(0) == "video/mp4") {
          Files.copy(result.getAHCResponse.getResponseBodyAsStream, Paths.get(newfilename.toString), StandardCopyOption.REPLACE_EXISTING)
        } else {
          Thread.sleep(10000)
          videoCheck(testId)
        }
      }

    isVideo(downloadUrl_F,testId)
    //isVideo(downloadUrl_R,testId,"after")
    //while( isVideo(downloadUrl_F,testId,"before") ) {}
    //while( isVideo(downloadUrl_R,testId,"after") ) {}
  }


  def getVideo(testId : String) = Action {
    val inline = true
    val content =  System.getProperty("user.home") + "/tmp/0/video/" + testId
    val testFile : File = new File(content)

    if(testFile.exists()) {
      Ok.sendFile(new File(content), inline)
    } else {
      Ok("false")
    }

  }

  def videoCheck(testId:String) : String = {

    val inline = true
    val content =  System.getProperty("user.home") + "/tmp/0/video/" + testId + ".mp4"
    val testFile : File = new File(content)

    if(testFile.exists()) {
      "true";
    } else {
      Tool.downloadVideo(testId)
      "false";
    }

  }

  def isVideo(testId : String) = Action {
    Ok(videoCheck(testId));
  }


// 프로젝트 새로고침
  def refreshProject(req_project_id: Int) = Action { implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")
    else {

      val projectInfo:Project = DBController.getProject(req_project_id)

      val simulTestId = WPTConnector(PathManager.deleteHTTP(projectInfo.domain)).toString
      val newporjectInfo = new Project(req_project_id, projectInfo.title, projectInfo.userId, projectInfo.domain, projectInfo.originIp, projectInfo.proxyuse, projectInfo.wptTestId, Some(simulTestId))

      DBController.modifyProject(newporjectInfo)
      Ok("Ok")

    }
  }


// 프록시캐쉬삭제
  def removeProxyCache(req_project_id: Int) = Action { implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")
    else {
      Ok("Ok")
    }
  }

// CDN캐쉬삭제
  def removeCDN(req_project_id: Int) = Action { implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")
    else {
      Ok("Ok")
    }
  }



  def getOptimizeList(req_project_id: Int)=DBAction{ implicit rs =>
    if(!SessionManager.checkProject(rs.session,req_project_id))
      Forbidden("does not have permission.")
    else {
      Ok(
        JsArray(
          OptimizeRules.tQuery.filter(_.projectId===req_project_id).list().map{rule=>
            Json.obj(
              "optimizeName"->
                (DBValue2OptimizeType(rule.optimizeType) match{
                  case CssSprite=>
                    "CSS Sprite"
                  case CssSpriteReverse=>
                    "CSS Sprite"
                  case Image=>
                    "Image Optimize"
                  case ImageReverse=>
                    "Image Optimize"
                  case Merge=>
                    "CSS/JS Merge"
                  case MergeRevserse=>
                    "CSS/JS Merge"
                  case Minify=>
                    "CSS.JS Minify"
                  case MinifyReverse=>
                    "CSS.JS Minify"
                  case Cdn=>
                    "CDN Upload"
                  case CdnReverse=>
                    "CDN Upload"
                }),
              "optimizeId" -> rule.optimizeId,
              "optimizeType" -> rule.optimizeType,
              "optimizeDate" -> rule.createDate.toString,
              "optimizedList" -> getOptimizedDetailList(rule.optimizeId,rule.optimizeType)
            )
          }
        )
      )
    }
  }

  def getOptimizedDetailList(optId:Int,optType:Int) :JsArray={
    val list = DB.withSession{implicit  session=>
      (DBValue2OptimizeType(optType) match {
        case CssSprite =>
          CssSpriteRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.objectId === _.objectId) map (_._2.originUrlStr)
        case CssSpriteReverse =>
          CssSpriteRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.objectId === _.objectId) map (_._2.originUrlStr)
        case Image =>
          ReplaceRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.fromObject === _.objectId) map (_._2.originUrlStr)
        case ImageReverse =>
          ReplaceRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.fromObject === _.objectId) map (_._2.originUrlStr)
        case Merge =>
          MergeRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.objectId === _.objectId) map (_._2.originUrlStr)
        case MergeRevserse =>
          MergeRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.objectId === _.objectId) map (_._2.originUrlStr)
        case Minify =>
          ReplaceRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.fromObject === _.objectId) map (_._2.originUrlStr)
        case MinifyReverse =>
          ReplaceRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.fromObject === _.objectId) map (_._2.originUrlStr)
        case Cdn =>
          ReplaceRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.fromObject === _.objectId) map (_._2.originUrlStr)
        case CdnReverse =>
          ReplaceRules.tQuery.filter(_.optimizeId === optId) innerJoin WebContents.tQuery on (_.fromObject === _.objectId) map (_._2.originUrlStr)
      }).list.map(JsString)
    }

    JsArray(list.toSeq)
  }


}