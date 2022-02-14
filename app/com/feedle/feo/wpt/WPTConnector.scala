package com.feedle.feo.wpt

import java.io.File

import com.feedle.feo.content.ObjectManager
import com.feedle.feo.util.PathManager
import com.feedle.feo.wpt.LocationType.Location
import controllers.{DBController => DB}
import play.Logger
import play.api.libs.json._
import play.api.libs.ws.WS

import scala.concurrent.Await
import scala.concurrent.duration._


/**
 * @see http://www.webpagetest.org/getLocations.php
 */
object LocationType extends Enumeration {
  type Location = LocationTypeVal

  class LocationTypeVal(val location: String, val abbreviation : String) extends Val(nextId)
  protected final def LocationValue(name: String, abbr : String): LocationTypeVal = new LocationTypeVal(name, abbr)

  val SEOUL_CHROME      = LocationValue("Seoul:Chrome", "SE")
  val SINGAPORE_IE8     = LocationValue("Singapore_IE8", "SP")
  val AMSTERDAM_CHROME  = LocationValue("Amsterdam_IISpeed:Chrome", "AD")
  val LOSANGELES_CHROME = LocationValue("LosAngeles:IE%2010", "LA")

  def getLocation(str : String) : Location= str.toLowerCase match {
    case x if x.contains("seoul")      => SEOUL_CHROME
    case x if x.contains("singapore")  => SINGAPORE_IE8
    case x if x.contains("amsterdam")  => AMSTERDAM_CHROME
    case x if x.contains("losangeles") => LOSANGELES_CHROME
  }

  def getLocationFromAbbr(abbr : String) : Location= abbr.toUpperCase match {
    case x if x.contains("SE") => SEOUL_CHROME
    case x if x.contains("SP") => SINGAPORE_IE8
    case x if x.contains("AD") => AMSTERDAM_CHROME
    case x if x.contains("LA") => LOSANGELES_CHROME
  }
}

/**
 * WebPageTestConnector
 * @see  https://sites.google.com/a/webpagetest.org/docs/private-instances
 * @note
 * 주의 : 한번실행되면 취소할수 없다!!!!!!왜냐면 취소하는 api를 쏘는걸 안 만들었기 때문이지! (api호출하면 wpt에서 호출한 순서대로 queue에 넣고 처리함)
 *     다시 받아오고싶으면 저장된 파일 및 db를 지우고 다시 WPTConnector( filePath )던지면 가져옴. 일단 걍 씁시다
 */
object WPTConnector {
  val STATUS_CODE = "StatusCode"
  val STATUS_PENDING = 100
  val STATUS_STARTED = 101
  val STATUS_OK = 200

  private[this] val RUNS = 2
  private[this] val FORMAT = "xml"
  private[this] val APIKEY = "e4ad9bd0f1c445b9bfafc3fca9b75964"
  private[this] val WEBPAGETEST = "http://www.webpagetest.org/runtest.php"


  /**
   * Seoul(Chrome), Singapore(IE8), Amsterdam(Chrome), LosAngeles(Chrome) 에서 WebPageTest 시행
   *
   * @param url
   * @return
   * SUCCESS (example):
   *  [{"ImagePath":"{HomeDir}/webPageTest/www.naver.com/imageSE.jpg"},
   *  [ {"Seoul:Chrome":{"firstViewLoadTime":2643,"repeatViewLoadTime":1751}},
   *  {"Singapore_IE8":{"firstViewLoadTime":4443,"repeatViewLoadTime":2820}},
   *  {"Amsterdam:Chrome":{"firstViewLoadTime":6710,"repeatViewLoadTime":4096}},
   *  {"LosAngeles:Chrome":{"firstViewLoadTime":3005,"repeatViewLoadTime":1012}}]
   *
   * FAIL :
   *  {"StatusCode":StatusCode}
   */
  def apply(url : String) : JsObject = {
    val allLocationResult = Json.obj(
      LocationType.SEOUL_CHROME.location      -> JsString(getRunTestId(url, LocationType.SEOUL_CHROME).get),
      LocationType.SINGAPORE_IE8.location     -> JsString(getRunTestId(url, LocationType.SINGAPORE_IE8).get),
      LocationType.AMSTERDAM_CHROME.location  -> JsString(getRunTestId(url, LocationType.AMSTERDAM_CHROME).get),
      LocationType.LOSANGELES_CHROME.location -> JsString(getRunTestId(url, LocationType.LOSANGELES_CHROME).get)
    )

    allLocationResult
  }

//  def allinOne(url : String) = {
//    val allLocationResult = Seq(
//      LocationType.SEOUL_CHROME      -> getXmlResult(url, LocationType.SEOUL_CHROME),
//      LocationType.SINGAPORE_IE8     -> getXmlResult(url, LocationType.SINGAPORE_IE8),
//      LocationType.AMSTERDAM_CHROME  -> getXmlResult(url, LocationType.AMSTERDAM_CHROME),
//      LocationType.LOSANGELES_CHROME -> getXmlResult(url, LocationType.LOSANGELES_CHROME)
//    )
//
//    getImageFile(allLocationResult(0)._2) match {
//      case file : File=>
//        Json.arr(
//          Json.obj("ImagePath" -> file.toString),
//          allLocationResult.map{ x =>
//            Json.obj(x._1.location -> Json.obj(
//              "firstViewLoadTime"  -> x._2.getAverageFirstViewLoadTime,
//              "repeatViewLoadTime" -> x._2.getAverageRepeatViewLoadTime
//            ))}
//        )
//
//      case code =>
//        Json.obj(STATUS_CODE -> code.toString)
//
//    }
//  }
  

  def getRunTestId(url : String, location : Location) : Option[String]= {
    val requestUrl = generateQuery(url, location)
    val newRunTestFile = PathManager.getWPT_FirstUri(url, location)

    println(newRunTestFile.toString)
    val wc = ObjectManager.wpt(newRunTestFile, requestUrl)
    val rt = new RunTest(wc.localPath)

    Some(rt.getTestId)
    //else None //throw new IllegalStateException("RunTest.xml's StatusCode is not 200. please check the wpt api")
  }

  def isStatusCode(rt : RunTest) : Boolean = {
    val sc = rt.getStatusCode

    if( sc == 200 ) true
    else false
  }


  def generateQuery (url : String, location: Location) = {
    def query : String = {
      val q = PathManager.deleteHTTP(url)
      "?url=" + q +
        "&runs=" + RUNS +
        "&f=" + FORMAT +
        "&k=" + APIKEY +
        "&location=" + location.location +
        "&video=1"
    }

    WEBPAGETEST + query
  }
  //http://www.webpagetest.org/runtest.php?url=www.naver.com&runs=2&f=xml&k=e4ad9bd0f1c445b9bfafc3fca9b75964&location=Amsterdam:Chrome


  def getImageFile(xmlResult : XmlResult) = {

    (xmlResult.xml \ "statusCode").text.toInt match {

      case STATUS_OK =>
        val imgUrlStr    : String = xmlResult.getScreenThumbLink1
        val newImageFile : File   = getImageName(xmlResult)

        ObjectManager.wpt(newImageFile, imgUrlStr)

      case x =>
        Logger.info("Error : StatusCode = " + x)
        x
    }
  }

  private[this] def getImageName(xmlResult: XmlResult): File = {

    val testUrl = xmlResult.getPureUrl
    val location = xmlResult.getLocation

    PathManager.getWPT_ImageUri(testUrl, location)
  }

  def getVideo (xmlResult : XmlResult) : Option[Seq[File]] = {

    (xmlResult.xml \ "statusCode").text.toInt match {

      case STATUS_OK =>
        val testId = xmlResult.getTestId
        val newVideoFile = getVideoName(xmlResult)

        val downloadUrl_F = "http://webpagetest.org/video/download.php?id="+testId+".1.0"
        val downloadUrl_R = "http://webpagetest.org/video/download.php?id="+testId+".1.1"
        val createVideoUrl_F = "http://www.webpagetest.org/video/create.php?tests="+testId+"-r:1-c:1&id="+testId+".1.0"
        val createVideoUrl_R = "http://www.webpagetest.org/video/create.php?tests="+testId+"-r:1-c:1&id="+testId+".1.1"

        Await.result(WS.url(createVideoUrl_F).get(), 5000 millis)
        Await.result(WS.url(createVideoUrl_R).get(), 5000 millis)

        def isVideo(url : String) = {
          val contentType = Await.result(WS.url(url).get(), 5000 millis).getAHCResponse.getHeaders("Content-Type")
          if ( contentType.toString == "video/mp4" ) true else false
        }

        while( isVideo(downloadUrl_F) && isVideo(downloadUrl_R) ) {}
        Some(Seq(newVideoFile(0), newVideoFile(1)))

      case x =>
        Logger.info("Error : StatusCode = " + x); None
    }
  }

  private[this] def getVideoName(xmlResult: XmlResult) = {

    val testUrl = xmlResult.getPureUrl
    val location = xmlResult.getLocation

    Seq( PathManager.getWPT_Video1Uri(testUrl, location), PathManager.getWPT_Video2Uri(testUrl, location) )
  }
}


case class XmlResult(fileStr : String) extends XR(xml.XML.loadFile(fileStr))
case class RunTest(fileStr : String)   extends RT(xml.XML.loadFile(fileStr))
abstract class XR(val xml : scala.xml.Elem) {

  /**
   * @return millisecond
   */
  def getAverageFirstViewLoadTime = ((((xml \\ "data") \ "average")\ "firstView") \ "loadTime").text.toInt
  def getAverageRepeatViewLoadTime = ((((xml \\ "data") \ "average")\ "repeatView") \ "loadTime").text.toInt

  //def getScreenThumb = (((((xml \\ "data") \ "run")\ "firstView") \ "thumbnails") \ "screenShot").text.trim
  // -> return "http://www.webpagetest.org/result/"+getTestId+"/1_screen_thumb.jpghttp://www.webpagetest.org/result/"+getTestId+"/2_screen_thumb.jpg"

  def getScreenThumbLink1 = "http://www.webpagetest.org/result/"+getTestId+"/1_screen_thumb.jpg"
  def getScreenThumbLink2 = "http://www.webpagetest.org/result/"+getTestId+"/2_screen_thumb.jpg"
  def getScreenThumbLink3 = "http://www.webpagetest.org/result/"+getTestId+"/1_Cached_screen_thumb.jpg"
  def getScreenThumbLink4 = "http://www.webpagetest.org/result/"+getTestId+"/2_Cached_screen_thumb.jpg"

  def getTestId = ((xml \\ "data") \ "testId").text.trim
  def getTestUrl = ((xml \\ "data") \ "testUrl").text.trim
  def getPureUrl = PathManager.deleteHTTP(getTestUrl)
  def getLocation = LocationType.getLocation(((xml \\ "data") \ "location").text.trim)
}

abstract class RT(val runTest:scala.xml.Elem) {
  def getTestId = ((runTest \\ "data") \ "testId").text.trim
  def getStatusCode = (runTest \\ "statusCode").text.toInt
}