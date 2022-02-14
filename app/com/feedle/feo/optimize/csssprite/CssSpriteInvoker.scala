package com.feedle.feo.optimize.csssprite

import com.feedle.feo.util.Util
import com.feedle.feo.weboject._
import com.phloc.css.decl.{CSSDeclaration, CSSExpression}
import controllers.{DBController => DB}
import models._
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag

import scala.collection.JavaConversions

/**
 * Created by infinitu on 2014. 4. 26..
 */
object CssSpriteInvoker {


  /**
   * Optimize를 실행한다.
   * @param optR 옵티마이즈 룰
   * @return 옵티마이즈를 실행해줄 Invoker
   */
  def optimize(optR:OptimizeRule)={

    //SpriteRule을 로딩
    val cssR = DB.getCssSpriteRule(optR.optimizeId)

    //로딩된 룰을 사용하기 쉽게 해쉬로 변환
    val objH = DB.getWebContent(cssR.map(_.objectId)).map(x=>x.objectId->x).toMap
    val urlH = cssR.map(x=>objH.get(x.objectId).get.originUrl->x).toMap

    //이 파일로 변경될 예정
    val dest : WebContent = DB.getWebContent(optR.result.get)

    (htmlobj:HtmlObject)=>{
      htmlobj.styleHash.foreach{x=>
        //CssBackground기반으로 검사
        val spritableCssBackgrounds =JavaConversions.asScalaBuffer(htmlobj.domTree.getAllElements)
          .map(htmlobj.styleHash.get)
          .filter(!_.isEmpty)
          .map{x=>CssBackground.extractBackgroundStyleRule(x.get)}
          .filter{x => CssBackground.canSprite(x)}
          .foreach{x=>
          //CSS Background 를 사용하고 있는 돔 엘리먼트
          val url = Util.AbsoluteURL(x.ref_path,x.background_image.substring(4,x.background_image.size-1))
          val ruleOpt = urlH.get(url)
          if(ruleOpt.isDefined){
            //그중에 대생이 되는 아이
            val rule = ruleOpt.get
            //CSS 변경
            optimizeCssBackground(x,rule,dest)
          }
        }
      }

      //img Tag를 모두 변경
      htmlobj.imgTagList.foreach{x=>
        val src = x.attr("src")
        val ruleOpt = urlH.get(Util.AbsoluteURL(htmlobj.webContent.originUrl,src))
        if(ruleOpt.isDefined){
          val rule = ruleOpt.get
          x.removeAttr("src")
          x.tagName("div")
          x.children().add(new Element(Tag.valueOf("span"),x.baseUri()))
          val backgStyle = "background:url('%s') %dpx %dpx;width:%dpx;height:%dpx".format(dest.originUrl.toString,-rule.posX,-rule.posY,rule.width,rule.height)
          x.attr("style",backgStyle)
        }
      }
    }
  }

  /**
   * 실제 CSS에 Sprite를 적용
   * @param back_info 변경할 Background의 정보
   * @param opt_info 변경 Rule
   * @param dest 목적이 되는 이미지
   */
  def optimizeCssBackground(back_info:CssBackgroundInfo,opt_info:CssSpriteRule,dest:models.WebContent){

    //적혀 있는 포지션
    val posString = back_info.background_position.split(" ")

    //현재 적용되어있는 포지션 파싱
    val oriX = CssBackground.guessLeftTopMargin(posString(0),opt_info.width,back_info.width).getOrElse(0)
    val oriY =
      if(posString.size>1)
        CssBackground.guessLeftTopMargin(posString(1),opt_info.height,back_info.height).getOrElse(0)
      else
        0


    //적용
    back_info.background_image_ref.decl.getProperty match{
      case "background_image" if back_info.background_position_ref == null =>
        back_info.background_image_ref.decl.setExpression(CSSExpression.createSimple("url(\""+dest.originUrl+"\")"))
        back_info.background_image_ref.addFunc(new CSSDeclaration("background_position",CSSExpression.createSimple(-opt_info.posX+"px "+(-opt_info.posY)+"px")))
      case "background_image"=>
        back_info.background_image_ref.decl.setExpression(CSSExpression.createSimple("url(\""+dest.originUrl+"\")"))
        back_info.background_position_ref.decl.setExpression(CSSExpression.createSimple((oriX-opt_info.posX)+"px "+(oriY-opt_info.posY)+"px"))
        back_info.background_image_ref.setChanged()
        back_info.background_position_ref.setChanged()
      case "background" if back_info.background_image_ref == back_info.background_position_ref=>
        back_info.background_image_ref.decl.setExpression(CSSExpression.createSimple("url(\""+dest.originUrl+"\") "+(oriX-opt_info.posX)+"px "+(oriY-opt_info.posY)+"px"))
        back_info.background_image_ref.setChanged()
      case "background"=>
        back_info.background_image_ref.decl.setExpression(CSSExpression.createSimple("url(\""+dest.originUrl+"\")"+ back_info.background_position + back_info.background_repeat))
        back_info.background_position_ref.decl.setExpression(CSSExpression.createSimple((oriX-opt_info.posX)+"px "+(oriY-opt_info.posY)+"px"))
        back_info.background_image_ref.setChanged()
        back_info.background_position_ref.setChanged()
      case _=>
    }

  }

  /**
   * deOptimize를 실행한다.
   * @param optR 옵티마이즈 룰
   * @return 옵티마이즈를 실행해줄 Invoker
   */
  def deoptimize(optR:OptimizeRule)={

    //CssBackground기반으로 검사
    val cssR = DB.getCssSpriteRule(optR.optimizeId)

    //사용하기 쉽도록 해쉬로 변환
    val objH = DB.getWebContent(cssR.map(_.objectId)).map(x=>x.objectId->x).toMap
    val urlH = cssR.map(x=>objH.get(x.objectId).get.originUrl->x).toMap

    //원래의 파일
    val dest: WebContent = DB.getWebContent(optR.result.get)

    (htmlobj:HtmlObject)=>{
      htmlobj.styleHash.foreach{x=>

        val spritableCssBackgrounds =JavaConversions.asScalaBuffer(htmlobj.domTree.getAllElements)
          .map{x=>
          htmlobj.styleHash.get(x)
        }.filter{x => !x.isEmpty} .map{x=>
          CssBackground.extractBackgroundStyleRule(x.get)
        }.filter{x => CssBackground.canSprite(x)}.filter {x=>
          val url = Util.AbsoluteURL(x.ref_path,x.background_image.substring(4,x.background_image.size-1))
          url == dest.originUrl
        }.foreach{x=>
          //적용되어있는 포지션
          val posString = x.background_position.split(" ")
          //현재의 포지션 파싱된버젼
          val oriX = CssBackground.guessLeftTopMargin(posString(0),0,x.width).getOrElse(0)
          val oriY =
            if(posString.size>1)
              CssBackground.guessLeftTopMargin(posString(1),0,x.height).getOrElse(0)
            else
              0
          //가장 가까이 있는 오브젝트를 찾아서 원래의 오브젝트를 추측함.
          def findOriginObject(idx:Int,opt_rule:CssSpriteRule,distance:Int):CssSpriteRule={
            if(cssR.size==idx) return opt_rule
            val disX = cssR(idx).posX+oriX
            val disY = cssR(idx).posY+oriY
            val dis = disX*disX + disY*disY
            if(disX>=0&&disY>=0&&dis<distance)
              return findOriginObject(idx+1,cssR(idx),dis)
            findOriginObject(idx+1,opt_rule,dis)
          }

          val originObj = findOriginObject(0,null,Integer.MAX_VALUE)
          optimizeCssBackground(x,originObj,objH.get(originObj.objectId).get)
        }

      }
    }
  }

  /**
   * 실제 CSS에 Sprite를 복원
   * @param back_info 변경할 Background의 정보
   * @param opt_info 변경 Rule
   * @param dest 목적이 되는 이미지
   */
  def deoptimizeCssBackground(back_info:CssBackgroundInfo,opt_info:CssSpriteRule,dest:models.WebContent){
    //현재 적용되어있는 포지션
    val posString = back_info.background_position.split(" ")

    //현재 적용되어있는 포지션 파싱
    val oriX = CssBackground.guessLeftTopMargin(posString(0),opt_info.width,back_info.width).getOrElse(0)
    val oriY =
      if(posString.size>1)
        CssBackground.guessLeftTopMargin(posString(1),opt_info.height,back_info.height).getOrElse(0)
      else
        0

    //변경
    back_info.background_image_ref.decl.getProperty match{
      case "background_image" if back_info.background_position_ref == null =>
        back_info.background_image_ref.decl.setExpression(CSSExpression.createSimple("url(\""+dest.originUrl+"\")"))
        back_info.background_image_ref.addFunc(new CSSDeclaration("background_position",CSSExpression.createSimple(opt_info.posX+"px "+opt_info.posY+"px")))
      case "background_image"=>
        back_info.background_image_ref.decl.setExpression(CSSExpression.createSimple("url(\""+dest.originUrl+"\")"))
        back_info.background_position_ref.decl.setExpression(CSSExpression.createSimple((oriX+opt_info.posX)+"px "+(oriY+opt_info.posY)+"px"))
        back_info.background_image_ref.setChanged()
        back_info.background_position_ref.setChanged()
      case "background" if back_info.background_image_ref == back_info.background_position_ref=>
        back_info.background_image_ref.decl.setExpression(CSSExpression.createSimple("url(\""+dest.originUrl+"\") "+(oriX+opt_info.posX)+"px "+(oriY+opt_info.posY)+"px"))
        back_info.background_image_ref.setChanged()
      case "background"=>
        back_info.background_image_ref.decl.setExpression(CSSExpression.createSimple("url(\""+dest.originUrl+"\")"+ back_info.background_position + back_info.background_repeat))
        back_info.background_position_ref.decl.setExpression(CSSExpression.createSimple((oriX+opt_info.posX)+"px "+(oriY+opt_info.posY)+"px"))
        back_info.background_image_ref.setChanged()
        back_info.background_position_ref.setChanged()
      case _=>
    }

  }

}
