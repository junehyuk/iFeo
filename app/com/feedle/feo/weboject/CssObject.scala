package com.feedle.feo.weboject

import com.phloc.css.reader.CSSReader
import com.phloc.css.writer.{CSSWriterSettings, CSSWriter}
import com.phloc.css.ECSSVersion
import scala.collection.mutable
import scala.collection.JavaConversions._
import com.phloc.css.decl.{CSSDeclaration, CSSStyleRule, CascadingStyleSheet}
import com.phloc.css.reader.errorhandler.DoNothingCSSParseErrorHandler
import java.io.File
import java.nio.charset.Charset
import java.net.URL
import org.jsoup.nodes.Element
import models.WebContent

/**
 *
 * Css 래퍼 클래스.
 * InnerCss, ExternalCss, InlineCss로 구분된다.
 * InnerCss는 <style>태그로 이루어진 HTML상에 기재된 CSS를 의미한다.
 * ExternalCss는 <link>,@import등으로 삽입된 외부CSS를 의미한다.
 * InlineCss는 Html의 style속성을 이용해 추가된 스타일 규칙을 의미한다.
 *
 * Created by infinitu on 2014. 4. 9..
 */

object CssObject {
  val CssWriter = new CSSWriter(new CSSWriterSettings(ECSSVersion.CSS30))

  /**
   * InnerCssObject를 생성한다.
   * @param content InnerCss의 내용
   * @param originUrl InnerCss를 포함하고 있는 HTML의 Url
   * @return 생성된 CssObject
   */
  def apply(content:String, originUrl:URL, projectId:Int) = new InternalCssObject(content, originUrl, projectId)

  /**
   * ExternalCss를 생성한다.
   * @param content ExternalFile의 WebContent
   * @return 생성된 CssObject
   */
  def apply(content: WebContent) = new ExternalCssObject(content)
}

abstract class CssObject{
  //phloc-css로 파싱된 CSS
  def parsedCSS:CascadingStyleSheet
  //external여부
  val isExternal:Boolean
  //import한 객체의 url
  val originURL:URL
  //변경사항 유무
  var isChanged:Boolean=false
  //이 Object를 소유하는 Project
  val projectId:Int

  //모든 StyleRule의 리스트
  lazy val styleRule = parsedCSS.getAllStyleRules
  //해당 CSS가 import하고 있는 CssObject
  lazy val importedCssObject:mutable.Buffer[ExternalCssObject]= parsedCSS.getAllImportRules.map{rule=>
    ObjectFactory.createCssObject(rule.getLocation.getURI,originURL,projectId)
  }

  /**
   * 하위 CSS를 포함한 모든 CSS의 StyleRule
   * @return styleRule의 리스트
   */
  def getAllStyleRules:mutable.Buffer[(CSSStyleRule,CssObject)] =
    importedCssObject.flatMap(_.getAllStyleRules) ++ styleRule.map(_->this)

  /**
   * 지정된 위치에 새로운 스타일 정의문을 삽입한다.
   * @param rule 새로운 정의문을 넣을 스타일 룰 객체
   * @param decl 삽입될 새로운 정의문
   */
  def addStyle(rule:CSSStyleRule)(decl:CSSDeclaration){
    rule.addDeclaration(decl)
    isChanged=true
  }

  /**
   * 변경사항이 있음을 체크한다..
   */
  def setChanged(){isChanged=true}
}


class ExternalCssObject(val webContent:WebContent) extends CssObject{
  //웹컨텐트로 부터 CSS를 파싱해온다.
  val mParsedCSS = CSSReader.readFromFile(new File(webContent.localPath),Charset.defaultCharset()
    ,ECSSVersion.CSS30,DoNothingCSSParseErrorHandler.getInstance())
  val isExternal = true
  val originURL=webContent.originUrl
  val projectId: Int = webContent.projectId

  def parsedCSS: CascadingStyleSheet = mParsedCSS
}

class InternalCssObject(val content:String, val originURL:URL,val projectId:Int) extends CssObject{
  val isExternal = false
  //String으로부터 CSS를 파싱한다.
  val mParsedCSS = CSSReader.readFromString(content,ECSSVersion.CSS30,DoNothingCSSParseErrorHandler.getInstance())

  def parsedCSS: CascadingStyleSheet = mParsedCSS
}


class InlineCssObject(val element:Element,val styleList:mutable.Buffer[CSSDeclaration],val originURL: URL) extends WebObject{
  var isChanged : Boolean = false

  /**
   * 해당 InlineCssObject에 새로운 정의문을 삽입한다.
   * @param decl 삽입할 새로운 정의문
   */
  def addStyle(decl:CSSDeclaration){
    styleList += decl
    isChanged=true
  }

  /**
   * 변경사항이 있음을 체크한다.
   */
  def setChanged(){isChanged=true}
}