package com.feedle.feo.optimize.merger

import com.feedle.feo.weboject._
import controllers.{DBController => DB}
import models._


object MergeInvoker {

  def optimize(optR:OptimizeRule):HtmlObject=>Unit={
    val jsR = DB.getMergeRule(optR.optimizeId).sortBy(_.index)

    val IndexList = jsR.map(_.index) zip DB.getWebContent(jsR.map(_.objectId))
    val originUrlList = IndexList.map(_._2.originUrl).toSet
    val originUrl_Index0 = IndexList.filter(_._1 == 0).head._2.originUrl

    println("ORIGIN_URL" + originUrl_Index0.toString + " ")
    val dest = DB.getWebContent(optR.result.get)

    (htmlobj:HtmlObject)=>{

      val jsList = htmlobj.jsHash.map(_._2.originURL).toSet
      val cssList = htmlobj.cssHash.map(_._2.originURL).toSet

      if ( (jsList intersect originUrlList) == originUrlList )
        htmlobj.jsHash
          .filter  { x => originUrlList contains x._2.originURL }
          .foreach { x =>
          if(x._2.originURL == originUrl_Index0) {
            x._1.attr("src", dest.originUrlStr)
          }
          else {
            x._1.remove()
          }
        }
      if ( (cssList intersect originUrlList) == originUrlList)
        htmlobj.cssHash
          .filter  { x => originUrlList contains x._2.originURL }
          .foreach { x =>
          if(x._2.originURL == originUrl_Index0) {
            x._1.attr("src", dest.originUrlStr)
          }
          else {
            x._1.remove()
          }
        }
    }
  }


  def deoptimize(optR:OptimizeRule):HtmlObject=>Unit={
    val jsR = DB.getMergeRule(optR.optimizeId).sortBy(_.index)

    val IndexList = DB.getInnerJoin_Merge(optR.optimizeId)
    val originUrlList = IndexList.map(_._2.originUrl).toSet

    val dest = DB.getWebContent(optR.result.get)

    (htmlobj:HtmlObject)=>{

      htmlobj.jsHash
        .filter  { x => originUrlList contains dest.originUrl }
        .foreach { x =>
        originUrlList.foreach{ url => htmlobj.addExternalJs(x._1, url.toString) }
      }
    }
  }
}

