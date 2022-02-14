package com.feedle.feo.optimize.csssprite

import java.net.URL
import javax.imageio.ImageIO

import com.feedle.feo.content.ObjectManager
import com.feedle.feo.optimize.csssprite.ImageType.ImageType
import com.feedle.feo.util.Util
import com.feedle.feo.weboject.{HtmlObject, ObjectFactory}
import models.WebContent

import scala.collection.{JavaConversions, mutable}


object SpriteableImageCrawler{
  /**
   * 테스트용 Url로 크롤링
   * @param url 테스트할 URL
   */
  def crawlSpritableImage(url:URL)={
    val htmlobj = ObjectFactory.createHtmlObject(url,"",0)
    cssBackgroundsWithMargin(htmlobj)++imgTagImagesWithMargin(htmlobj)
  }

  /**
   * HtmlObject로 크롤링
   * @param htmlobj 크롤 할 Object
   * @return 크롤링 결과
   */
  def crawlSpritableImage(htmlobj:HtmlObject)={
    //CSS결과와 imgTag를 합침.
    cssBackgroundsWithMargin(htmlobj)++imgTagImagesWithMargin(htmlobj)
  }

  /**
   * CssBackground를 크롤링함.
   * @param htmlobj 크롤할 대상
   * @return 발견된 이미지와 그 이미지의 프리셋 margin
   */
  def cssBackgroundsWithMargin(htmlobj:HtmlObject):mutable.Buffer[ImageWithMargin]={
    val spritableCssBackgrounds =JavaConversions.asScalaBuffer(htmlobj.domTree.getAllElements)
      .map{x=>
      htmlobj.styleHash.get(x)

    }.filter{x => !x.isEmpty} .map{x=>
      CssBackground.extractBackgroundStyleRule(x.get)
    }.filter{x => CssBackground.canSprite(x)}

    val hash = mutable.HashMap[URL,mutable.Buffer[CssBackgroundInfo]]()


    spritableCssBackgrounds.foreach{ x=>
      val url = Util.AbsoluteURL(x.ref_path,x.background_image.substring(4,x.background_image.size-1))
      var optBuf = hash.get(url)
      if(optBuf.isEmpty){
        optBuf = Some(mutable.ArrayBuffer[CssBackgroundInfo]())
        hash+=url->optBuf.get
      }
      val buf = optBuf.get
      buf+=x
    }

    CssBackground.parseCSSBackground(htmlobj.webContent.projectId,hash)
  }

  /**
   * img tag를 파싱
   * @param htmlobj 파싱할 htmlobject
   * @return 발견된 이미지와 그 이미지의 프리셋 margin
   */
  def imgTagImagesWithMargin(htmlobj:HtmlObject):mutable.Buffer[ImageWithMargin]={

    val spritableImg = mutable.Buffer[ImageWithMargin]()

    htmlobj.imgTagList.foreach{img=>
      val src = Util.AbsoluteURL(htmlobj.webContent.originUrl,img.attr("src"))
      if(!src.toString.equals("")){
        //val widthStr = img.attr("width")
        //val heightStr = img.attr("height")
        //todo Issue :: width,height attribute있을경우 처리.

        val imageWebContent = ObjectManager(Seq(src), htmlobj.webContent.projectId)(0).get
        println(imageWebContent.localPath)
        val image = ImageIO.read(imageWebContent.bodyInputStream)
        val widthImg = image.getWidth
        val heightImg = image.getHeight

        spritableImg += new ImageWithMargin(imageWebContent,new Margin(0,0,0,0,widthImg,heightImg),ImageType.ImgTag)
      }
    }

    spritableImg
  }
}

/**
 * 프리셋 margin
 * margin이란, Css Sprite에서 공백으로 표현되는 부분으로 이 영역 사이에는 다른 이미지가 들어가서는 안된다.
 */
case class Margin(top : Int, right : Int, left : Int, bottom : Int, width : Int , height : Int) {
  val isBottomInfinite = bottom<0
}

object ImageType extends Enumeration {
  type ImageType = Value
  val CssBackground, SpritedBackground, ImgTag = Value
}

/**
 * margin과 webContent의 래퍼클래스
 */
case class ImageWithMargin(content : WebContent,
                           margin : Margin,
                           imageType : ImageType)

