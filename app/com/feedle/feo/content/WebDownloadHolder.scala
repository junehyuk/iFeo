package com.feedle.feo.content

import java.io.File
import java.net.URL
import java.nio.file.{Files, Paths, StandardCopyOption}

import com.feedle.feo.util.PathManager
import com.ning.http.client.Response
import models._
import play.Logger
import play.api.libs.ws.WS

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._



class WebDownloadHolder(val requestUrl : URL, var requestMap : RequestHeader=new RequestHeader) {
  if ( requestMap == null) requestMap = new RequestHeader

  private[content] def getSync(pid : Int) : Option[WebContent] = Await.result(getAsync(pid), 3000 millis)
  private[content] def getAsync(pid : Int) : Future[Option[WebContent]] = {
    val localPath = PathManager.getFile(requestUrl, pid)
    getAsyncAt(localPath, pid)
  }

  private[content] def getSyncAt(dest : WebContent) : Option[WebContent] = Await.result(getAsyncAt(dest), 3000 millis)
  private[content] def getAsyncAt(dest : WebContent) : Future[Option[WebContent]] = getAsyncAt(new File(dest.localPath), dest.projectId)

  private[content] def getSyncAt(newLocalPath : File, pid : Int) : Option[WebContent] = Await.result(getAsyncAt(newLocalPath, pid), 3000 millis)
  private[content] def getAsyncAt(newLocalPath : File, pid : Int) : Future[Option[WebContent]] = future {
    val ws = WS.url(requestUrl.toString)

    // put request header
    requestMap.foreach{ case(key, value) => ws.withHeaders(key->value.mkString(";"))}
    // put query
    if(requestUrl.getQuery!=null) requestUrl.getQuery.split('&').foreach{x=>
      val sp=x.split('=')
      ws.withQueryString(sp(0)->sp(1))
    }

    //get result
    val result = Await.result(ws.withFollowRedirects(false).get(),3000 millis)
    if ( result.status/10 == 40 ) None

    if (result.ahcResponse.isRedirected) {
      new WebContentInsertHolder().get( Await.result(ws.get(),3000 millis).ahcResponse.getUri.toURL, requestMap, pid)
    }
    else {
      val resultResponse = result.getAHCResponse
      val redirectUrl = resultResponse.getUri.toURL
      val responseMap = new ResponseHeader + resultResponse.getHeaders

      //save in local
      newLocalPath.getParentFile.mkdirs()
      val byteSize = Files.copy(resultResponse.getResponseBodyAsStream, Paths.get(newLocalPath.toString), StandardCopyOption.REPLACE_EXISTING)
      Logger.info(requestUrl + " is downloaded in " + Paths.get(newLocalPath.toString) + ".\n")

      Some(new WebContent(0, pid, redirectUrl.toString, newLocalPath.toString, responseMap.getContentTypeNum,
        Some(byteSize), Some(requestMap.toString), Some(responseMap.toString), PathManager.isGenFile(newLocalPath), false))
    }
  }

  def postSyncAt(dest : WebContent, query : immutable.Map[String,Seq[String]]) : (WebContent, Response) =
    Await.result(postAsyncAt(dest, query), 45000 millis)

  def postAsyncAt(dest : WebContent, query : immutable.Map[String,Seq[String]]) : Future[(WebContent, Response)] = future {
    val ws = WS.url(requestUrl.toString)
    val newLocalPath = new File(dest.localPath)
    requestMap.foreach{ case(key, value) => ws.withHeaders(key->value.mkString(","))}

    val resultResponse = Await.result(ws.post(query),30000 millis).getAHCResponse

    val responseMap = new ResponseHeader + resultResponse.getHeaders

    newLocalPath.getParentFile.mkdirs()
    val byteSize = Files.copy(resultResponse.getResponseBodyAsStream, Paths.get(newLocalPath.toString), StandardCopyOption.REPLACE_EXISTING)
    Logger.info(requestUrl + " is downloaded in " + Paths.get(newLocalPath.toString) + ".\n")

    (dest, resultResponse)
  }
}