package com.feedle.feo.optimize.csssprite

import java.net.URL
import javax.imageio.ImageIO

import com.feedle.feo.content.ObjectManager
import com.feedle.feo.weboject.CssObject
import com.feedle.feo.weboject.HtmlObject.CssDeclarationWithObject

import scala.collection.mutable

/**
 *
 * 파싱된 CSS를 받아 정의문을 파싱하여 필요한 테이터를 뽑아낸다.
 *
 * Created by infinitu on 2014. 4. 10..
 */
object CssBackground{

  //정의가 되어있지 않을때에 기본 설정값.
  val default_background_image: String = "none"
  val default_background_position: String = "0% 0%"
  val default_background_repeat: String = "repeat"
  val default_background_size: String = "auto"
  val default_width: String = "auto"
  val default_height: String = "auto"

  /**
   * position을 파싱하기위한 regex
   */
  val positionPattern=("(left|right|center|(-|)[0-9]+(px|%|in|cm|mm|em|ex|pt|pc))" +
                       "( (top|bottom|center|(-|)[0-9]+(px|%|in|cm|mm|em|ex|pt|pc))|)").r
  /**
   * imageUrl을 파싱하기위한 regex
   */
  val imageUrlPattern= """url\(("|'|)([^)"']+)("|'|)\)""".r

  /**
   * repeat를 파싱하기위한 regex
   */
  val repeatPattern="(no-repeat|repeat(-x|-y|))".r

  /**
   * 스타일 룰셋에서 background, width, height를 파싱
   * @param rulSet 파싱할 룰셋
   * @return 파싱된 background info
   */
  def extractBackgroundStyleRule(rulSet:mutable.Buffer[CssDeclarationWithObject])={

    var background_image: String = default_background_image
    var background_position: String = default_background_position
    var background_repeat: String = default_background_repeat
    var background_size: String = default_background_size
    var width: String = default_width
    var height: String = default_height
    var ref_path: URL = null
    var background_image_ref: CssDeclarationWithObject = null
    var background_position_ref:CssDeclarationWithObject = null

    rulSet.foreach{rule=>
      val expressionStr = CssObject.CssWriter.getCSSAsString(rule.decl.getExpression)
      rule.decl.getProperty match{
        case "background"=>
          //background의 경우 여러 속성이 합쳐져 있기 때문에 적용이 가능하다.
          val image = imageUrlPattern.findFirstIn(expressionStr).getOrElse(default_background_image)
          val position = positionPattern.findFirstIn(expressionStr).getOrElse(default_background_position)
          val repeat = repeatPattern.findFirstIn(expressionStr).getOrElse(default_background_repeat)
          background_image=image
          background_position=position
          background_repeat=repeat
          background_size=default_background_size
          ref_path = rule.url
          background_image_ref=rule
          background_position_ref=rule

        case "background-image"=>
          background_image=expressionStr
          ref_path = rule.url
          background_image_ref=rule
        case "background_position"=>
          background_position=expressionStr
          background_position_ref=rule
        case "background_repeat"=>  background_repeat=expressionStr
        case "background_size"=>    background_size=expressionStr
        case "width"=>              width=expressionStr
        case "height"=>             height=expressionStr
        case _=>
      }
    }
    new CssBackgroundInfo(
      background_image,
      background_position,
      background_repeat,
      background_size, width,
      height,
      ref_path,
      background_image_ref,
      background_position_ref
    )
  }

  /**
   * Sprite할 수 있는가 검사를 한다.
   * repeat가 있거나, url이 아니거나, base64로 인코딩된것은 불가하다.
   * @param obj 검사할  background info
   * @return t/f
   */
  private[csssprite] def canSprite(obj: CssBackgroundInfo): Boolean = {
    if (obj.background_repeat == "repeat") return false
    if (!obj.background_image.toLowerCase.contains("url")) return false
    if (obj.background_image.contains(";base64,")) return false
    true
  }

  /**
   * px를 파싱하기 위한 regex
   */
  val pxPattern = "((-|)[0-9]+)px".r
  /**
   * %를 파싱하기위한 regex
   */
  val percentPattern ="((-|)[0-9]+)%".r

  /**
   * Left 와 Top Margin을 추측한다.
   * todo issue :: inherit, initial 처리.
   * @param str 파싱할 스트링
   * @param imageLength 적용할 image의 크기 (width 혹은 height)
   * @param targetLength 적용될 Dom의 크기 (width 혹은 height)
   * @return 추측한 값
   */
  def guessLeftTopMargin(str:String, imageLength:Int, targetLength:String):Option[Int]={
    var percent=1.0f
    str match{
      case pxPattern(pxStr,_)=>
        return Some(pxStr.toInt)
      case "center"=>percent=0.5f
      case "top"=>return Some(0)
      case "bottom"=> percent=1.0f
      case "left"=> return Some(0)
      case "right"=> percent=1.0f
      case percentPattern(pxStr,_)=>
        percent= pxStr.toFloat /100
    }

    val tarMatch=pxPattern.findFirstMatchIn(targetLength)
    if(tarMatch.isEmpty) return None
    Some((tarMatch.get.group(1).toFloat*percent).toInt)
  }

  /**
   * Right 와 Bottom Margin을 추측한다.
   * todo issue :: inherit, initial 처리.
   * @param str 파싱할 스트링
   * @param imageLength 적용할 image의 크기 (width 혹은 height)
   * @param targetLength 적용될 Dom의 크기 (width 혹은 height)
   * @return 추측한 값
   */
  def guessRightBottomMargin(str:String, imageLength:Int, targetLength:String):Option[Int]={

    val tarMatch=pxPattern.findFirstMatchIn(targetLength)
    val tarLen = if(tarMatch.isEmpty) -1 else tarMatch.get.group(1).toInt

    str match{
      case pxPattern(pxStr,_)=>
        if(tarLen<0)
          None
        else
          Some(tarLen - pxStr.toInt)

      case "center"=>
        if(tarLen<0)
          None
        else
          Some((tarLen-imageLength)/2)
      case "top"=>
        if(tarLen<0)
          None
        else
          Some(tarLen-imageLength)

      case "bottom"=>
        Some(0)

      case "left"=>
        if(tarLen<0)
          None
        else
          Some(tarLen-imageLength)

      case "right"=>
        Some(0)

      case percentPattern(pxStr,_)=>
        val percent= pxStr.toFloat /100
        if(percent==1) return Some(0)
        if(tarLen<0)
          None
        else
          Some(((tarLen-imageLength)*percent).toInt)

    }
  }

  /**
   * CssBackground Info를 받아 실제 Margin으로 파싱/연산 해준다.
   * @param infoMap 연산할 CssBackgroundInfo
   * @return 연산결과
   */
  def parseCSSBackground(project_id:Int,infoMap:mutable.HashMap[URL,mutable.Buffer[CssBackgroundInfo]])={
    val imageList = infoMap.map{node=>
      val url = node._1
      val buf = node._2
      val imageWebContent = ObjectManager(url, project_id).get
      val image = ImageIO.read(imageWebContent.bodyInputStream)
      val width = image.getWidth
      val height = image.getHeight

      if(buf.size<2)
      {
        val target = buf(0)
        val posString = target.background_position.split(" ")

        val margin_left = guessLeftTopMargin(posString(0),width,target.width).getOrElse(-1)
        val margin_top =
          if(posString.size>1)
            guessLeftTopMargin(posString(1),height,target.height).getOrElse(-1)
          else
            0

        val margin_right = guessRightBottomMargin(posString(0),width,target.width).getOrElse(-1)

        val margin_bottom =
          if(posString.size>1)
            guessRightBottomMargin(posString(1),height,target.height).getOrElse(-1)
          else
            guessRightBottomMargin("0px",height,target.height).getOrElse(-1)

        val margin = new Margin(margin_top,margin_right,margin_left,margin_bottom,width,height)
        new ImageWithMargin(imageWebContent,margin,ImageType.CssBackground)

      }
      else
      {
        //todo CSS Sprite된것. 분해하고 재조립 할 수 있었으면 좋겠다.
        val margin = new Margin(0,0,0,0,width,height)
        new ImageWithMargin(imageWebContent,margin,ImageType.SpritedBackground)
      }
    }
    imageList.toBuffer
  }
}

/**
 * CssBackground 정보를 분리해서 저장해놓은 케이스 클래스
 */
case class CssBackgroundInfo(background_image: String,
                             background_position: String,
                             background_repeat: String,
                             background_size: String,
                             width:String,
                             height:String,
                             ref_path:URL,
                             background_image_ref:CssDeclarationWithObject,
                             background_position_ref:CssDeclarationWithObject)
