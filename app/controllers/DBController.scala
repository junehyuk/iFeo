package controllers

import java.io.File
import java.net.URL

import models._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{DB, Session}
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc.Controller

import scala.slick.lifted.{CanBeQueryCondition, Column}

object DBController extends Controller{
  def JsonResult(inner : JsObject) : JsObject = {Json.obj("result" -> inner)}
  def JsonResult(inner : String)   : JsObject = {Json.obj("result" -> inner)}
  def JsonError(msg : String)      : JsObject = {Json.obj("error"->msg)}
  def JsonOk                       : JsObject = JsonResult("OK")

  def getObjectId(localPath : String) : Int = DB.withSession { implicit session:Session=>
    WebContents.tQuery.where(_.localPath === localPath).first.objectId
  }

  //case class WebContent(objectId: Int, pid: Int, originUrlStr: String, newLocalPath: String, contentTypeValue: Int, request: Option[String], response: Option[String], isgen: Boolean) {
  //  def addWebContent(pid : Int, originUrl : URL, localFileName : String, content)                                                                //todo

  /**
   * id / email 중복 검사 후에 json 반환
   * @param obj
   * @return
   *       SUCCESS : { "result" : "OK" }
   *       FAIL : {"error" : "Email Exists"}
   *              {"error" : "Id Exists"}
   */
  def join(obj:User):JsObject=DB.withSession{implicit session:Session=>
    if      (isExistWhere(_.email === obj.email))    JsonError("Email Exists")
    else if (isExistWhere(_.id === obj.id))          JsonError("Id Exists")
    else                                             (Users.tQuery returning Users.tQuery.map(_.userId)) += obj ; JsonOk
  }

  def getMember(id:String) : User = DB.withSession{ implicit session:Session =>
    Users.tQuery.filter(_.id === id).first
  }

  // 회원정보 수정부분
  def modify(obj:User)= DB.withSession { implicit session =>
    Users.tQuery.filter(_.id === obj.id).map{x=>(x.pwd, x.email)}.update((obj.pwd, obj.email))
  }

  //프로젝트 가져오기 (projectNumber(int) 입력)
  def getProject(projectId:Int) : Project = DB.withSession{ implicit session:Session =>
    Projects.tQuery.filter(_.projectId === projectId).first
  }

  def getProxy(projectId:Int) : ProxySetting = DB.withSession{ implicit session:Session =>
    ProxySettings.tQuery.filter(_.projectId === projectId).first
  }

  //프로젝트 추가 (pid 리턴 ㅋㅋ)
  def addProject(obj:Project)=DB.withSession{implicit session:Session=>
    (Projects.tQuery returning Projects.tQuery.map(_.projectId)) += obj
  }

  //프로젝트 추가 (pid 리턴 ㅋㅋ)
  def addProxy(obj:ProxySetting)=DB.withSession{implicit session:Session=>
    (ProxySettings.tQuery returning ProxySettings.tQuery.map(_.settingId)) += obj
  }


  //프로젝트 수정
  def modifyProject(obj:Project)=DB.withSession{implicit session:Session=>
    Projects.tQuery.filter(_.projectId === obj.projectId).update(obj)
  }

  //프록시 수정
  def modifyProxy(obj:ProxySetting)=DB.withSession{implicit session:Session=>
    ProxySettings.tQuery.filter(_.projectId === obj.projectId).update(obj)
  }

  //페이지 가져오기 (pageNumber(int) 입력)
  def getPage(pageId:Int) : Page = DB.withSession{ implicit session:Session =>
    Pages.tQuery.filter(_.pageId === pageId).first
  }

  //프로젝트 목록
  def listProject(userid:Int)=DB.withSession{implicit session:Session=>
    Json.arr(
      Projects.tQuery.filter(_.userId === userid).list.map{ x =>
        Json.obj(
          "pid" -> x.projectId ,
          "title"     -> x.title,
          "domain"    -> x.domain,
          "originIp"  -> x.originIp,
          "proxyuse"  -> x.proxyuse
        )})
  }

  // 프로젝트의 페이지 목록 JSON으로 던져도됌
  def listPage(projectId:Int) =DB.withSession{implicit session:Session=>
    JsArray(
      Pages.tQuery.filter(_.projectId === projectId).list.map { x=>
        //        (pageId: Int, projectId: Int, path: String, header: Option[String], query: Option[String])
        Json.obj(
          "pageId"    -> x.pageId,
          "projectId" -> x.projectId,
          "path"      -> x.path,
          "header"    -> x.header,
          "query"     -> x.query
        )
      }
    )
  }


  // 프로젝트 삭제
  def removeProject(projectid:Int)=DB.withSession{implicit session:Session=>
    Projects.tQuery.filter(_.projectId === projectid).delete
  }


  // 페이지 추가 (Project ID등.. 정보는 Page오브젝트에 입력함)
  def addPage(obj:Page)=DB.withSession{implicit session:Session=>
    val pageid = Pages.tQuery returning Pages.tQuery.map(_.pageId) += obj
    val newPage = new Page(pageid,obj.projectId,obj.path,obj.header,obj.query)
    val newHost = Projects.tQuery.filter(_.projectId === obj.projectId).first()
    PageHtmlManager.refreshPage(newPage,newHost.domain)
  }


  // 페이지 삭제
  def removePage(pageid:Int)=DB.withSession{implicit session:Session=>
    Pages.tQuery.filter(_.pageId === pageid).delete
  }


  /**
   * id 혹은 Email과 pwd를 받아서 login 처리
   * @param idOrEmail
   * @param pwd
   * @return
   *       SUCCESS : { "result" : "OK" }
   *       FAIL : {"error" : "Id/Email Not Exists"}
   *              {"error" : "Id/Email And Pwd Mismatch"}
   */
  def login(idOrEmail:String , pwd : String) : JsObject = DB.withSession{ implicit session:Session =>
    def p[T<: Column[_]:CanBeQueryCondition](wt : (Users)=>T) :JsObject = DB.withSession{ implicit session :Session =>

      if (Users.tQuery.filter(wt).filter(_.pwd === pwd).firstOption.isEmpty)
        JsonError("Id/Email And Pwd Mismatch")

      else
        JsonOk
    }

    if   (isExistWhere(_.email === idOrEmail))      p(_.email===idOrEmail)
    else if (isExistWhere(_.id === idOrEmail))      p(_.id === idOrEmail)
    else                                            JsonError("Id/Email Not Exists")
  }

  def isExistWhere[T<: Column[_]:CanBeQueryCondition](wt : (Users)=>T) :Boolean = DB.withSession { implicit session:Session =>
    if (Users.tQuery.filter(wt).firstOption == None) false else true
  }

  def getWebContent(url : URL, pid : Int) : Option[WebContent] = DB.withSession { implicit session =>
    WebContents.tQuery.where(_.projectId === pid)
      .where(_.originUrlStr === url.toString).firstOption
  }

  def getWebContent(localPath : File, pid : Int) : Option[WebContent] = DB.withSession { implicit session =>
    WebContents.tQuery.where(_.projectId === pid).where(_.localPath === localPath.toString).firstOption
  }


  // local_gen_file (replace)
  def getWebContent(filePaths : Seq[File], pid :Int) = DB.withSession { implicit session =>
    //    val filePaths = fileNames.map{x=>PathManager.getGenFilePath(pid, x)}
    val q = for(
      w <- WebContents.tQuery.where(_.projectId === pid) if w.localPath inSetBind filePaths.map(_.toString)
    ) yield w
    q.list
  }

  def getWebContents(urls : Seq[URL], pid : Int) : Seq[WebContent]=DB.withSession{implicit session:Session=>
    val q = for {
      w <- WebContents.tQuery.where(_.projectId === pid) if w.originUrlStr inSet urls.map(_.toString)
    } yield w
    q.list
  }
  def getWebContent(objectIds : Seq[Int]) : Seq[WebContent] = DB.withSession{implicit session:Session=>
    val q = for {
      w <- WebContents.tQuery if w.objectId inSet objectIds
    } yield w
    q.list
  }
  def getWebContent(objectId : Int) : WebContent = DB.withSession { implicit session =>
    WebContents.tQuery.where(_.objectId === objectId).first
  }



  // with out cdn webContent
  def getAllWebContent(pid : Int) = DB.withSession { implicit session =>
    JsArray(
      WebContents.tQuery.where(_.projectId === pid).where(_.iscdn === false).list.map { x =>
        Json.obj(
          "objectId" -> x.objectId,
          "projectId" -> x.projectId,
          "originUrlStr" -> x.originUrlStr,
          "localPath" -> x.localPath,
          "contentTypeValue" -> x.contentTypeValue,
          "request" -> x.request,
          "response" -> x.response,
          "isgen" -> x.isgen,
          "iscdn" -> x.iscdn
        )
      }
    )
  }

  def getAllWebContentWithCdn(pid : Int) = DB.withSession { implicit session =>
    val innerJoin = for (
      wc <- WebContents.tQuery.where(_.iscdn === true)
        innerJoin WebContents.tQuery.where(_.iscdn === false) on (_.localPath === _.localPath)
    ) yield wc

    JsArray(
      innerJoin.list.map{x=> x._2}.map { x =>
        Json.obj(
          "objectId" -> x.objectId,
          "projectId" -> x.projectId,
          "originUrlStr" -> x.originUrlStr,
          "localPath" -> x.localPath,
          "contentTypeValue" -> x.contentTypeValue,
          "request" -> x.request,
          "response" -> x.response,
          "isgen" -> x.isgen,
          "iscdn" -> x.iscdn
        )
      }
    )
  }

  //proxy file setting정보 가져옴
  def getAllWebContentWithProxyFileSetting(pid : Int) = DB.withSession { implicit session =>

    val result = ProxyFileSettings.tQuery.filter(_.projectId === pid).list

    JsArray(
      result.map { x=>
        Json.obj(
          "objectId" -> x.objectId,
          "cacheControl" -> x.cacheControl,
          "refreshCycle" -> x.refreshCycle,
          "proxyCache"   -> x.proxyCache
        )}
    )

  }

  //과연 제대로 리턴될 것인가!!ㅋㅋ
  def addWebContents(objs : Seq[WebContent]) : Seq[WebContent] = DB.withSession { implicit session =>
    val seqInt = WebContents.tQuery returning WebContents.tQuery.map(_.objectId) ++= objs
    (objs zip seqInt).map { x=> x._1.setObjectId(x._2) }
  }
  def addWebContent(obj : WebContent) : WebContent = DB.withSession { implicit session =>
    obj.setObjectId( WebContents.tQuery returning WebContents.tQuery.map(_.objectId) += obj)
  }
  def addWebContent(pid: Int,
                    originUrlStr: String,
                    localPath: String,
                    contentTypeValue: Int,
                    size : Option[Long],
                    request: Option[String],
                    response: Option[String],
                    isgen: Boolean,
                    iscdn : Boolean=false ) : WebContent = DB.withSession { implicit session =>
    val newWC = new WebContent(0, pid, originUrlStr, localPath, contentTypeValue, size, request, response, isgen, iscdn)
    newWC.setObjectId( WebContents.tQuery returning WebContents.tQuery.map(_.objectId) += newWC)
  }


  def rmWebContent(objId : Int) = DB.withSession { implicit session =>
    WebContents.tQuery.filter(_.objectId === objId).delete
  }

  def getCssSpriteRule(optimizeId : Int) = DB.withSession{implicit session=>
    CssSpriteRules.tQuery.filter(_.optimizeId === optimizeId).list
  }
  def getMergeRule(optimizeId : Int) = DB.withSession{implicit session=>
    MergeRules.tQuery.filter(_.optimizeId === optimizeId).list
  }
  def getReplaceRule(optimizeId : Int) : ReplaceRule = DB.withSession { implicit session =>
    ReplaceRules.tQuery.filter(_.optimizeId === optimizeId).first
  }

  def getInnerJoin_Merge(optimizeId : Int) : Seq[(MergeRule, WebContent)] = DB.withSession { implicit session =>
    val implicitInnerjoin = for {
      merge <- MergeRules.tQuery.filter(_.optimizeId === optimizeId)
      wc    <- WebContents.tQuery      if merge.objectId === wc.objectId
    } yield (merge, wc)
    implicitInnerjoin.list
  }

  /**
   * ProxySettingXml에서 사용. pid에 대한 ProxySetting/ ProxyFileSetting 을 리턴
   * @param pid
   * @return
   */
  def getInnerJoin_ProxySetting (pid : Int) : (Option[((Project,ProxySetting), Seq[(ProxyFileSetting,WebContent)])])= DB.withSession{ implicit session =>
    val project_ProxySetting = for {
      p <- Projects.tQuery.where(_.projectId===pid) innerJoin ProxySettings.tQuery on (_.projectId === _.projectId)
    } yield p

    val fileSetting_webContent = for {
      f <- ProxyFileSettings.tQuery innerJoin WebContents.tQuery on (_.objectId === _.objectId)
    } yield f

    val pp = project_ProxySetting.list().headOption

    val fw = fileSetting_webContent.list()

    if (pp.isEmpty)
      None
    else {
      val result = (pp.get._1, pp.get._2)
      if (fw.isEmpty)
        Some(result, Nil)
      else
        Some(result, fw)
    }
  }

  def addProxyFileSetting(projectId: Int, cacheControl: Option[String], refreshCycle: Option[Int], proxyCache: Option[Boolean], autoProxy: Option[String]) = DB.withSession { implicit session =>
    ProxyFileSettings.tQuery += new ProxyFileSetting(0, projectId, cacheControl, refreshCycle, proxyCache, autoProxy)
  }


}

