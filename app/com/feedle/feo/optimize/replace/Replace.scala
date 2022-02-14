package com.feedle.feo.optimize.replace

import java.io.{File, PrintWriter}

import com.feedle.feo.content.ObjectManager
import com.feedle.feo.util.{ContentType, PathManager}
import com.feedle.feo.weboject._
import com.phloc.css.ECSSVersion
import com.phloc.css.writer.CSSWriter
import models._
import org.jsoup.nodes.{DataNode, Element}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.collection.mutable

/**
 * Replace(특정 파일을 다른 파일로 대체하는 Optimize)Rule을 생성한다.
 */
object Replace {

  val CSSWriter = new CSSWriter(ECSSVersion.CSS30,false)
  CSSWriter.setContentCharset("UTF-8")

  /**
   * CSS변경사항을 Html/CSS에 적용하고 외부 파일을 경우에는 새로운 파일로 만들고 ReplaceRule을 추가한다.
   * @param optimizeId 변경을 일으킨 Optimize의 ID
   * @param htmlobj 검사를 할 페이지
   */
  def applyCssChanges(optimizeId:Int)(htmlobj:HtmlObject){
    val newreplaceBuf = mutable.Buffer[HtmlObject=>Unit]()
    htmlobj.cssHash.foreach{css:(Element,CssObject)=>css._2 match{
      case internal:InternalCssObject=>
        internal.importedCssObject.foreach(cssChangeDetect)
        if(internal.isChanged){

          val newCssStr = CSSWriter.getCSSAsString(internal.parsedCSS)
          css._1.remove()
          css._1.appendChild(new DataNode(newCssStr,htmlobj.webContent.originUrl.toString))
        }
      case external:ExternalCssObject=>
        cssChangeDetect(external)
    }}

    /**
     * External Css의 변경사항을 감지한다.
     * @param cssobj 감지할 대상
     */
    def cssChangeDetect(cssobj:ExternalCssObject){
      cssobj.importedCssObject.foreach(cssChangeDetect)

      if(!cssobj.isChanged)
        return

      val replaceFileName = PathManager.createReplaceName(ContentType.CSS)
      val newobj = ObjectManager(replaceFileName, cssobj.webContent.projectId).get

      CSSWriter.writeCSS(cssobj.parsedCSS,new PrintWriter(new File(newobj.localPath)))

      val newReplaceRule = new ReplaceRule(0,htmlobj.webContent.projectId,optimizeId,cssobj.webContent.objectId,newobj.objectId)
      DB.withSession{implicit session=>
        WebContents.tQuery.where(_.objectId === newobj.objectId).update(newobj.setSize(newobj.localFile.length()))
        ReplaceRules.tQuery += newReplaceRule
      }

      val newReplaceFunc = ReplaceInvoker.optimize(newReplaceRule)
      newReplaceFunc(htmlobj)
      newreplaceBuf += newReplaceFunc
    }
  }
}
