package com.feedle.feo.optimize.merger

import java.net.URL

import com.feedle.feo.content.{ObjectManager, WebDownloadHolder}
import com.feedle.feo.util.{ContentType, PathManager, Util}
import com.yahoo.platform.yui.compressor.YUICSSCompressorMerge
import models.{RequestHeader, WebContent}
import org.apache.http.HttpHeaders

import scala.xml.MalformedAttributeException

class Merger(val objs: Seq[Seq[WebContent]]) {
  var size : Long = -1
  val origin_dest : Map[Seq[WebContent], WebContent] = objs.map{x=>
  //head 기준으로 contentType, pid가 같은것을 고름
    val cType = x.head.contentType
    val pid = x.head.projectId
    val filterX = x.filter(cType == _.contentType).filter(pid == _.projectId)
    val md5 = Util.getMD5(filterX.toString())

    filterX-> ObjectManager(PathManager.createMergeName(md5, cType), pid).get
  }.toMap

  origin_dest.foreach{ x=>
    x._1.head.contentType match {
      case ContentType.JS => downloadJsfromClosure(x._1, x._2)
      case ContentType.CSS => downloadCSSfromYUI(x._1, x._2)
      case _ => throw new IllegalArgumentException
    }
  }
  /**
   * https://developers.google.com/closure/compiler/docs/api-ref
   * @throws MalformedAttributeException
   * @return
   */
  def downloadJsfromClosure(objs:Seq[WebContent], dest:WebContent) = {
    val queryMap = Util.createClosureQueryMap(objs.map(_.originUrl))
    val req = new RequestHeader().+= ((HttpHeaders.CONTENT_TYPE, Seq("application/x-www-form-urlencoded")))
    val (rWC, response) = new WebDownloadHolder(new URL("http://closure-compiler.appspot.com/compile"), req).postSyncAt(dest, queryMap)

    if("error".equals(response.getResponseBodyExcerpt(5, "utf-8").toLowerCase))
      throw new MalformedAttributeException(response.getResponseBody)

    size = rWC.localFile.length()
  }

  def downloadCSSfromYUI(obj:Seq[WebContent], dest:WebContent) = {
    val option : Array[String] =Array("-o", dest.localPath)
    val elements = obj.map(_.localPath)

    YUICSSCompressorMerge.start((elements ++: option).toArray)

    size = dest.localFile.length()
  }
}
