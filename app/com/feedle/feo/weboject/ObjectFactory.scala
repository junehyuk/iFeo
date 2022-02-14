package com.feedle.feo.weboject

import java.net.URL

import com.feedle.feo.content.ObjectManager
import com.feedle.feo.util.Util
import models.RequestHeader

/**
 *
 * Css/Js/Html 오브젝트를 생성하는 팩토리 오브젝트
 *
 * Created by infinitu on 2014. 4. 10..
 */
object ObjectFactory {

  def createHtmlObjectWithString(url:String,originUrl:URL,queryStr:String="",headerStr:String="",projectId:Int):HtmlObject=
    createHtmlObject(Util.AbsoluteURL(originUrl,url+"?"+queryStr),headerStr,projectId)
  def createHtmlObject(url:URL,headerStr:String="",projectId:Int):HtmlObject={

    HtmlObject(ObjectManager(url, new RequestHeader + headerStr, projectId).get)
  }

  def createInternalJsObject(content:String,originUrl:URL, projectId:Int):InternalJsObject=JsObject(content,originUrl, projectId)
  def createJsObject(url:String,originUrl:URL, projectId:Int):ExternalJsObject=createJsObject(Util.AbsoluteURL(originUrl,url), projectId)
  def createJsObject(url:URL, projectId:Int):ExternalJsObject=JsObject(ObjectManager(url, projectId).get)

  def createInternalCssObject(content:String,originUrl:URL, projectId:Int):InternalCssObject=CssObject(content,originUrl, projectId)
  def createCssObject(url:String,originUrl:URL, projectId:Int):ExternalCssObject=createCssObject(Util.AbsoluteURL(originUrl,url),projectId)
  def createCssObject(url:URL, projectId:Int):ExternalCssObject=CssObject(ObjectManager(url, projectId).get)

}
