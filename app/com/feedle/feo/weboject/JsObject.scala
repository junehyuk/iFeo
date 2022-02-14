package com.feedle.feo.weboject

import models.WebContent
import java.net.URL

/**
 * 자바스크립트 래퍼 클래스.
 */
object JsObject {
  def apply(content:String, originURL:URL, projectId:Int) = new InternalJsObject(content, originURL, projectId:Int)
  def apply(content:WebContent) = new ExternalJsObject(content)
}

abstract class JsObject{
  val content:String
  val isExternal:Boolean
  val originURL:URL
  var isChanged:Boolean = false
}

class InternalJsObject(val content:String, val originURL:URL, projectId:Int) extends JsObject{
  val isExternal = false
  def setChanged() {isChanged = true}
}

class ExternalJsObject(val webContent:WebContent) extends JsObject{
  val isExternal = true
  val content= ""
  val originURL:URL = webContent.originUrl
}
