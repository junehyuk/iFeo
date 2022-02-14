package com.feedle.feo.optimize.minifier

import java.net.URL

import com.feedle.feo.content.{ObjectManager, WebDownloadHolder}
import com.feedle.feo.util.{ContentType, PathManager, Util}
import com.yahoo.platform.yui.compressor.YUICSSCompressorMinify
import models.{RequestHeader, WebContent}
import org.apache.http.HttpHeaders

import scala.xml.MalformedAttributeException



class Minifier(val objs: Seq[WebContent]) {
  val origin_dest : Seq[(WebContent, WebContent)] = objs.map{x=> x -> ObjectManager(PathManager.getMinName(x.localPath), x.projectId).get}.toSeq

  origin_dest.foreach { x =>
    x._1.contentType match {
      case ContentType.JS => downloadJsfromClosure(x._1, x._2)
      case ContentType.CSS => downloadCSSfromYUI(x._1, x._2)
      case _ => throw new IllegalArgumentException
    }}

  /**
   * https://developers.google.com/closure/compiler/docs/api-ref
   * @throws MalformedAttributeException
   * @return
   */
  def downloadJsfromClosure(obj:WebContent, dest:WebContent) : WebContent= {
    val queryMap = Util.createClosureQueryMap(obj.originUrl)
    val req = new RequestHeader().+= ((HttpHeaders.CONTENT_TYPE, Seq("application/x-www-form-urlencoded")))
    val (rWC, response) = new WebDownloadHolder(new URL("http://closure-compiler.appspot.com/compile"), req).postSyncAt(dest, queryMap)

    if("error".equals(response.getResponseBodyExcerpt(5, "utf-8").toLowerCase))
      throw new MalformedAttributeException(response.getResponseBody)

    rWC
  }

  /**
   * @see http://yui.github.io/yuicompressor/css.html
   * @param obj
   * @param dest
   */
  def downloadCSSfromYUI(obj:WebContent, dest:WebContent) = {
    val option : Array[String] =Array("-o", dest.localPath)
    val elements = obj.localPath
    //requestUrls.map{ x => WebDownloader(x, projectId).getSync.localPath }

    YUICSSCompressorMinify.start(elements +: option)
  }
}
