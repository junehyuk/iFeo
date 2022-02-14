package com.feedle.feo.util

import java.net.URL
import java.security.{NoSuchAlgorithmException, MessageDigest}
import scala.collection.{immutable, mutable}
import java.io.File

object Util {
  /**
   * full url regex pattern
   */
  val urlPattern = "((http)|(https)|.+)://.+".r
  /**
   * url path regex pattern
   */
  val pathPattern = "(/)(.*)".r
  /**
   * url scheme regex pattern
   */
  val schemePattern = "(//)(.*)".r

  /**
   * 상대주소를 절대주소로 바꿈.
   * @param hostUrl 상대주소를 로드한 URL
   * @param subUrl 상대주소
   * @return 절대주소
   */
  def AbsoluteURL(hostUrl:URL,subUrl:String):URL = subUrl match {
    case urlPattern(_,_,_)=>new URL(subUrl)
    case schemePattern(mark,url)  => new URL(hostUrl.getProtocol+"://"+url)
    case pathPattern(mark,paths)  => new URL(hostUrl.getProtocol,hostUrl.getHost,hostUrl.getPort,subUrl)
    case _          =>
      val pathStack = mutable.Stack[String]()
      hostUrl.getPath.split('/').foreach{x=>pathStack push x}
      if(!pathStack.isEmpty)pathStack.pop()
      subUrl.split('/').foreach {
        case ".." => if (!pathStack.isEmpty) pathStack.pop()
        case "." =>
        case x => pathStack push x
      }

      val pathBuilder = mutable.StringBuilder.newBuilder
      pathStack.reverse.foreach{x=>if(!x.equals("")){pathBuilder.append("/");pathBuilder.append(x)}}

      new URL(hostUrl.getProtocol,hostUrl.getHost,hostUrl.getPort,pathBuilder.mkString)
  }

  def createClosureQueryMap(requestUrls : Seq[URL]) : immutable.Map[String,Seq[String]] = {
    immutable.Map(
      "code_url"          -> requestUrls.map{x=>x.toString},
      "compilation_level" -> Seq("WHITESPACE_ONLY"),
      "output_format"     -> Seq("text"),
      "output_info"       -> Seq("compiled_code", "warnings", "errors") )
  }
  def createClosureQueryMap(requestUrl : URL) : immutable.Map[String,Seq[String]]
  = createClosureQueryMap(Seq(requestUrl))

  def getMD5(str : String) : String = {
    var MD5 = ""
    try{
      val md = MessageDigest.getInstance("MD5")
      md.update(str.getBytes)
      val byteData: Array[Byte] = md.digest()
      val sb = new StringBuffer()
      (0 to byteData.length - 1) foreach { i =>
        sb.append(Integer.toString((byteData(i)&0xff) + 0x100, 16).substring(1))
      }
      MD5 = sb.toString

    }catch{
      case e : NoSuchAlgorithmException => e.printStackTrace(); MD5=null
    }
    MD5
  }

}

/**
 * DB에 들어가는 ContentType정의 및 ContentType으로 변환/복원
 */
object ContentType extends Enumeration {
  type ContentType = ContentTypeVal
  class ContentTypeVal(val name: String,val value : Int) extends Val(nextId, name)
  protected final def Value(name: String, value : Int): ContentTypeVal = new ContentTypeVal(name,value)

  val ANY = Value("*/*", -1)
  val HTML = Value("text/html", 0)
  val CSS = Value("text/css", 1)
  val JS = Value("application/javascript", 2)
  val IMAGE = Value("image/", 3)


  def getContentType(file : File) : ContentType = getContentType(file.toString)
  def getContentType(filePath : String) : ContentType = {
    val slash = filePath.lastIndexOf('/')
    val fileName = filePath.substring(if(slash == -1) 0 else slash) + "   "
    val dot = fileName.lastIndexOf('.')
    fileName.substring(dot+1,dot+4).toLowerCase match {
      case "htm" => ContentType.HTML
      case "css"  => ContentType.CSS
      case "js "  => ContentType.JS
      case "js@"  => ContentType.JS
      case "png"  => ContentType.IMAGE
      case "jpg"  => ContentType.IMAGE
      case _ => ContentType.ANY
    }
  }
  //getNumFromContentType(getContentType(filePath))
  /**
   * 0:text/html etc..
   * 1:text/css etc...
   * 2.application/javasctipt etc...
   * 3:image/_
   * -1: etc
   * @param contentType
   */

  def getNumFromContentType(contentType:String) : Int = contentType.toLowerCase match {
    case x if x.contains("html") => ContentType.HTML.value
    case x if x.contains("css") => ContentType.CSS.value
    case x if x.contains("stylesheet") => ContentType.CSS.value
    case x if x.contains("javascript") => ContentType.JS.value
    case x if x.contains("image") => ContentType.IMAGE.value
    case _ => ContentType.ANY.value
  }


  def getMalformedTypeFromNum(num:Int) : ContentType = num match {
    case ContentType.HTML.value => ContentType.HTML
    case ContentType.CSS.value => ContentType.CSS
    case ContentType.JS.value => ContentType.JS
    case ContentType.IMAGE.value => ContentType.IMAGE
    case ContentType.ANY.value => ContentType.ANY
    case _ => ContentType.ANY
  }

}

/**
 * DB에 들어가는 OptmizeType를 정의 및 OptimizeType변환/복원
 */
object OptimizeType extends Enumeration{
  type OptimizeType = OptimizeTypeValue

  class OptimizeTypeValue(val dbValue:Int) extends Val(nextId)

  val CssSprite = new OptimizeType(1)
  val CssSpriteReverse = new OptimizeType(-1)
  val Image = new OptimizeType(2)
  val ImageReverse = new OptimizeType(-2)
  val Merge = new OptimizeType(3)
  val MergeRevserse = new OptimizeType(-3)
  val Minify = new OptimizeType(4)
  val MinifyReverse = new OptimizeType(-4)
  val Cdn = new OptimizeType(5)
  val CdnReverse = new OptimizeType(-5)

  implicit def OptimizeType2DBValue(optType:OptimizeType) = optType.dbValue
  implicit def DBValue2OptimizeType(dbVal:Int) = dbVal match {
    case CssSprite.dbValue=>CssSprite
    case CssSpriteReverse.dbValue=>CssSpriteReverse
    case Image.dbValue=>Image
    case ImageReverse.dbValue=>ImageReverse
    case Merge.dbValue=>Merge
    case MergeRevserse.dbValue=>MergeRevserse
    case Minify.dbValue=>Minify
    case MinifyReverse.dbValue=>MinifyReverse
    case Cdn.dbValue=>Minify
    case CdnReverse.dbValue=>CdnReverse


  }

}