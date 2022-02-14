package com.feedle.feo.optimize.replace

import com.feedle.feo.weboject._
import com.feedle.feo.util.{ContentType, Util}
import scala.collection.JavaConversions._
import com.phloc.css.decl.CSSURI
import models.ReplaceRule
import models.OptimizeRule
import controllers.{DBController=>DB}


/**
 * 기존에 있는 Replace Rule을 DB로 부터 가져와서 Html/Css에 적용하는 작업
 */
object ReplaceInvoker {

  /**
   * 적용
   * @param replaceR 적용할 ReplaceRule
   * @return ReplaceRule을 로딩하고, HtmlObject를 옵티마이즈 해주는 Invoker를 리턴한다.
   */

  def optimize(optR: OptimizeRule) : HtmlObject=>Unit= {
    val replaceR = DB.getReplaceRule(optR.optimizeId)
    optimize(replaceR)
  }

  def optimize(replaceR:ReplaceRule):HtmlObject=>Unit={
    val contents = DB.getWebContent(Seq(replaceR.fromObject,replaceR.destObject))
    val fromC = contents(0)
    val destC = contents(1)

    //종류에 따라 변경방식이 변함.
    fromC.contentType match {
      case ContentType.CSS =>
        (htmlobj: HtmlObject) => {
          //Html에 직접 기술된 External Css를 먼저 바꿈.
          //이것부터 바꿔야 이미 변경사항이 적용되어 등록된 CSS를 또 바꾸지 않게됨.
          htmlobj.cssHash.filter { x => x._2 match {
            case external: ExternalCssObject =>
              external.webContent.originUrl.equals(fromC.originUrl)
            case _ =>
              false
          }}.foreach{x =>
            x._1.attr("href", destC.originUrl.toString)
            htmlobj.cssHash -= x._1
            htmlobj.cssHash += x._1 -> ObjectFactory.createCssObject(destC.originUrl,htmlobj.webContent.projectId)
            htmlobj.recentStyleHash=null
          }

          //다음은 CssObject들이 Import한 애들을 바꿈.
          htmlobj.cssHash.foreach(x=>replaceImportRules(x._2))

          //모든 CSSRule에 Import된 CssObject를 탐색함.
          def replaceImportRules(cssobj:CssObject){
            cssobj.parsedCSS.getAllImportRules
              .filter(_.getLocation equals fromC.originUrl.toString)
              .foreach{x=>
                x.setLocation(new CSSURI(destC.originUrl.toString))
                val idx = cssobj.importedCssObject.indexWhere(_.webContent.originUrl equals fromC.originUrl)
                val newCss = ObjectFactory.createCssObject(destC.originUrl,htmlobj.webContent.projectId)
                cssobj.importedCssObject remove idx
                cssobj.importedCssObject.add(idx,newCss)
                cssobj.setChanged()
                htmlobj.recentStyleHash=null
              }
          }


        }
      case ContentType.JS =>
        (htmlobj: HtmlObject) => {
          //JS의 경우 단순히 External만 바꿔주는 형태.
          htmlobj.jsHash.filter { x => x._2 match {
            case external: ExternalJsObject =>
              external.webContent.originUrl.equals(fromC.originUrl)
            case _ =>
              false
          }
          }.foreach(x => x._1.attr("src", destC.originUrl.toString))
        }

      case ContentType.IMAGE =>
        (htmlobj: HtmlObject) => {
          //Image의 경우 Img Tag와 Background 로 나눠서 변경

          //
          htmlobj.imgTagList
            .filter(x => Util.AbsoluteURL(htmlobj.webContent.originUrl, x.attr("src")).equals(fromC.originUrl))
            .foreach(_.attr("src", destC.originUrl.toString)) //TODO RelativeURL

          //CSSBackground
          htmlobj.styleHash.foreach(_._2.foreach { x =>
            x.decl.getExpression.getAllMembers.foreach {
              case uri: CSSURI =>
                if (Util.AbsoluteURL(x.url, uri.getURI) == fromC.originUrl) {
                  uri.setURI(destC.originUrl.toString)
                  x.setChanged()
                }
              case _ =>
            }
          })
        }
      case _=>
        (htmlobj: HtmlObject) => {}
    }
  }


}
