package com.feedle.feo.content

import java.io.File
import java.net.URL

import com.feedle.feo.util.{ContentType, PathManager}
import com.feedle.feo.wpt.{RunTest, WPTConnector}
import controllers.{DBController => DB}
import models.{RequestHeader, WebContent}

import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.duration._


private[content] class WebContentInsertHolder {

  val WebDownloadMapper = mutable.HashMap.empty[URL,Future[Option[WebContent]]]


  // urls with no requestHeader
  def get(requestUrls : Seq[URL], projectId : Int) : Seq[Option[WebContent]] = {
    requestUrls.map { url =>
      get(url, null, projectId)
    }.toSeq
  }

  //normal
  def get(requestUrl : URL, headerMap : RequestHeader=new RequestHeader, projectId : Int) : Option[WebContent] = {
    def enqueue() : Future[Option[WebContent]] = {
      val result = new WebDownloadHolder(requestUrl, headerMap).getAsync(projectId)
      WebDownloadMapper += (requestUrl -> result )
      result
    }
    def dequeue() = {
      WebDownloadMapper -= requestUrl
    }

    val webContentOption = DB.getWebContent(requestUrl, projectId)
    if ( webContentOption.isEmpty ) {
      //find mapper
      val newobj = Await.result(WebDownloadMapper.getOrElse(requestUrl, enqueue()), 100000 millis)
      dequeue()
      if (newobj.isDefined) DB.addWebContent(newobj.get)
      newobj
    }
    else webContentOption
  }

  //gen
  //gen 에는 WebDownloadHolder가 붙지 않음
  //filename은 반드시 확장자가 있다.
  def gen(fileName : String, pid : Int ) : Option[WebContent]= {
    val webContentOption = DB.getWebContent(new File(PathManager.getGenFilePath(pid, fileName)), pid)
    if ( webContentOption.isEmpty ) {
      Some(DB.addWebContent( pid,
                     PathManager.getNewURL(pid, fileName),
                     PathManager.getGenFilePath(pid, fileName),
                     ContentType.getContentType(fileName).value,
                     None,
                     None,
                     None,
                     PathManager.GEN,
                     false))
    }
    else webContentOption
  }



  //gen
//  def gens(files : Seq[File], pid : Int ) : Seq[(WebContent, String)]= {
//    // db insert
//    // local file - gen local file
//    val genfilePaths : Seq[String] = PathManager.getGenFilePaths(pid, files)
//    val gen_local : Seq[(String, File)] = genfilePaths zip files
//
//    val alreadyExistWebContent = DBController.getWebContentsF(genfilePaths)
//
//    gen_local.filter{x=> alreadyExistWebContent.contains(x._1)}.map{x=> ()}
//  }

  //WPT외에는 사용불가
  //for WebPageTestApi
  def wptRunTest(file : File, requestUrl : String) : WebContent= {
    if (!PathManager.isInWPTDIR(file)) return null

    val pid = 0 // 'cause wpt projectId is 0
    val webContentOption = DB.getWebContent(file, pid)

    def download = {
      play.Logger.info(requestUrl + " WPT 호출 ")
      new WebDownloadHolder(new URL(requestUrl),new RequestHeader).getSyncAt(file, pid)
    }

    if ( webContentOption.isEmpty ) {
      download

      DB.addWebContent( pid,
        requestUrl,
        file.toString,
        ContentType.getContentType(file).value,
        None,
        None,
        None,
        PathManager.GEN,
        false)
    }
    else  webContentOption.get
  }

  private def isStatusCodeOK(rt : RunTest) : Boolean = {
    rt.getStatusCode == WPTConnector.STATUS_OK
  }

  private def isTodayDate(rt : RunTest) : Boolean = {
    val format = new java.text.SimpleDateFormat("yyMMdd")
    val date = format.format(new java.util.Date())

    date equals rt.getTestId.substring(0, 6)
  }
}
