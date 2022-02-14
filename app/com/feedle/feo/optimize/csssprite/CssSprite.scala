package com.feedle.feo.optimize.csssprite

import java.awt.image.BufferedImage
import scala.collection.mutable
import javax.imageio.ImageIO
import java.io.File
import java.awt.Graphics
import java.net.URL
import com.feedle.feo.weboject.HtmlObject
import models.WebContent
import controllers.{ DBController => DB }


/**
 * Css Sprite Rule을 생성하는 오브젝트
 */
object CssSprite {

  case class Position(x :Int, y : Int)

  val imagePositionMap = mutable.HashMap[(WebContent,Margin),Position]()
  val intervalPixel = 5

  /**
   * CSS내부에 사용하는 이미지들의 정보를 입력하면 Sprite된 이미지를 생성.
   * @param combinedImageFile 합친 파일의 이름.
   * @param files 이미지 정보틀 (margin등)
   * @return 이미지 배치 정보
   */
  def createImage(combinedImageFile : File, files : List[(Int,Margin)])={

    val idList = files.map(_._1)
    val objectList:Seq[WebContent] = DB.getWebContent(idList)

    val srcFile=
      for{
        i<-objectList
        j<-files if i.objectId == j._1
      } yield i->j._2

    //nextImagePos 는 다음에 이미지가 들어가야할 곳을 추론한 위치.
    var nextImagePosY = 0
    var nextImagePosX = 0
    var maxHeightOfHrizontal = 0
    var maxWidthOfVertical = 0
    var previousImageBottom = Int.MaxValue
    var previousImageRight= Int.MaxValue

    //세로로 일자로 배열
    srcFile foreach { file =>
      val margin = file._2
      if( !margin.isBottomInfinite ) {
        if(previousImageBottom<margin.top)
          nextImagePosY+=  margin.top-previousImageBottom

        imagePositionMap+=file->new Position(nextImagePosX,nextImagePosY)

        nextImagePosY += margin.bottom + intervalPixel+margin.height
        previousImageBottom = margin.bottom

        //일렬로 붙이는 것중에 너비가 제일 넓은것을 구함.
        if(maxWidthOfVertical<margin.width)
          maxWidthOfVertical=margin.width
      }
    }

    //bottom이 무한대일경우 가로로 배치.
    srcFile foreach { file =>
      val margin = file._2
      if( margin.isBottomInfinite ) {
        if(previousImageRight<margin.left)
         nextImagePosX+=margin.left-previousImageRight

        imagePositionMap+=file->new Position(nextImagePosX,nextImagePosY)

        nextImagePosX += margin.right+intervalPixel+margin.width
        previousImageRight = margin.right

        if(maxHeightOfHrizontal<margin.height)
          maxHeightOfHrizontal=margin.height
      }
    }

    //todo Issue::마지막줄에 top margin이 그 위쪽의 bottom margin보다 클경우를 예상하지 못함.
    //todo Issue::마지막줄에서 Left or Right가 무한대일경우는 예상하지 못함.

    //마지막 전체 사진 크기
    val canvasHeight:Int = nextImagePosY+maxHeightOfHrizontal
    val canvasWidth:Int = math.max(nextImagePosX-previousImageBottom,maxWidthOfVertical)

    // create image
    // 이때 TYPE_4BYTE_ABGR등의 옵션으로 화질을 줄 수 있다.
    //combine시 gif, jpg의 경우는 BufferedImage.TYPE_INT_RGB의 파라미터를 사용해야하고(jpg 경우 배경이 검은색)
    //png의 경우에는 BufferedImage.TYPE_INT_ARGB라고 명시해주어야 배경이 투명하게 나옴!
    //default로 png로 잡고 개발
    val combinedImage : BufferedImage = new BufferedImage( canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB )
    // merge image
    val mergeGraphics : Graphics = combinedImage.getGraphics
    imagePositionMap.foreach { image =>
      mergeGraphics.drawImage(ImageIO.read(new File(image._1._1.localPath)),
        image._2.x, image._2.y, null)
    }

    //save
    combinedImageFile.getParentFile.mkdirs()
    ImageIO.write(combinedImage, "PNG", combinedImageFile)

    imagePositionMap
  }

  /**
   * 테스트용 웹을 크롤링 해와서 생성함.
   * @param pageUrl 테스트할 URL
   */
  def crawlWeb(pageUrl:URL)={
    SpriteableImageCrawler.crawlSpritableImage(pageUrl)
  }

  /**
   * sprite가능한 모든 이미지를 찾는다.
   * @param htmlobj 검색학 HtmlObject
   * @return 검색된 ImageWithMargin
   */
  def findSpritableImages(htmlobj:HtmlObject)=SpriteableImageCrawler.crawlSpritableImage(htmlobj)

  /**
   * sprite가능한 모든 이미지를 찾는다.
   * @param htmlobjs 검색학 HtmlObject List
   * @return 검색된 ImageWithMargin
   */
  def findSpritableImages(htmlobjs:List[HtmlObject])={
    htmlobjs.map(SpriteableImageCrawler.crawlSpritableImage)
  }


}


