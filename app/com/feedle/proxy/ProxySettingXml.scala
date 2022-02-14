package com.feedle.proxy

import controllers.{DBController => DB}
import models._

/**
 * Created by YeonjuMac on 2014. 6. 13..
 */

object GlobalRuleType extends Enumeration{
  type GlobalRuleType = Value

  val CONTENT_ENCODING = "contentEncoding"
  val JS_MINIFY       = "jsMinify"
  val CSS_MINIFY      = "cssMinify"
  val REPLACE_404     = "replace404"
  val FAVICON         = "favicon"
  val CACHE_HEADER_TYPE = "cacheHeaderType"
  val CACHE_HEADER_VALUE = "cacheHeaderValue"
}


class ProxyXml(pid : Int) {
  val innerjoinList : ((Project,ProxySetting), Seq[(ProxyFileSetting,WebContent)])= DB.getInnerJoin_ProxySetting(pid)
    .getOrElse(throw new IllegalAccessException("프로젝트에 프록시 세팅 파일이 없을걸?"))

  val project      : Project        = innerjoinList._1._1
  val proxySetting : ProxySetting   = innerjoinList._1._2
  val fileSettings  : Seq[(ProxyFileSetting,WebContent)] = innerjoinList._2

  //////file rule //////////
  //object_id
  //project_id
  //cache_control
  //refresh_cycle
  //proxy_cache


  def toXml = <feedleproxyConfiguration host={project.domain} port="80" origin={project.originIp}>
        <GlobalRule name= {GlobalRuleType.CONTENT_ENCODING}   value={proxySetting.contentEncoding.get}/>
        <GlobalRule name= {GlobalRuleType.JS_MINIFY}          value={proxySetting.jsMinify.getOrElse(false).toString}/>
        <GlobalRule name= {GlobalRuleType.CSS_MINIFY}         value={proxySetting.cssMinify.getOrElse(false).toString}/>
        <GlobalRule name= {GlobalRuleType.REPLACE_404}        value={proxySetting.replace404.getOrElse(false).toString}/>
        <GlobalRule name= {GlobalRuleType.FAVICON}            value={proxySetting.favicon.getOrElse(-1).toString}/>
        <GlobalRule name= {GlobalRuleType.CACHE_HEADER_TYPE}  value={proxySetting.headercacheType.get}/>
        <GlobalRule name= {GlobalRuleType.CACHE_HEADER_VALUE} value={proxySetting.headercacheDay.get}/>

        <RuleGroup>
          {fileSettings.map{ file =>
            <file path={ file._2.originUrl.getPath } >
              <config name ="contentType"  value = { file._2.contentTypeValue.toString }/>
              {if (file._1.proxyCache.isDefined)
                <config name="proxyCache" value={file._1.proxyCache.get.toString}/>}
              {if (file._1.cacheControl.isDefined)
                <config name="cacheControl" value={file._1.cacheControl.get}/>}
              {if (file._1.proxyCache.isDefined)
                <config name="refresh_cycle" value={file._1.refreshCycle.get.toString}/>}
              {if (file._1.proxyCache.isDefined)
                <config name="auto_proxy" value={file._1.autoProxy.get}/>}
            </file>
        }}
        </RuleGroup>
      </feedleproxyConfiguration>


}

//////global rule//////////
//setting_id
//project_id
//content_encoding
//jsminify
//cssminify
//replace404
//favicon
//headercache_day
//headercache_type

//////file rule //////////
//object_id
//project_id
//cache_control
//refresh_cycle
//proxy_cache


