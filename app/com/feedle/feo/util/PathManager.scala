package com.feedle.feo.util

import java.net.URL
import java.io.File
import com.feedle.feo.util.ContentType.ContentType
import com.feedle.feo.wpt.LocationType
import LocationType.Location


object PathManager {

  private val basicURI = System.getProperty("user.home") + "/tmp/"         /*  " ~/tmp/ " */
  private val genDefaultURI = "gen-file/"

  val 임시프로젝트 = 1
  val WPTid = 0

  val GEN = true
  val NotGEN = false

  def defaultFileURI(pid : Int) = basicURI + pid+ "/"                    /*   " ~/tmp/{pid}/ "    */
  def getGenURI(pid : Int) = defaultFileURI(pid) + genDefaultURI                 /*   " ~/tmp/{pid}/gen-file/ "     */
  def getGenFilePath(pid: Int, file : File) = getGenURI(pid) + file.getName
  def getGenFilePath(pid: Int, fileName : String) = getGenURI(pid) + fileName    /*   " ~/tmp/{pid}/gen-file/{filename} "   */
  def getGenFilePaths(pid: Int, files : Seq[File]) = files.map{x=> getGenURI(pid)+x.getName}    /*   " ~/tmp/{pid}/gen-file/{filename} "   */
  def isGenFile(file : File) : Boolean = file.toString.contains(basicURI) && file.toString.contains(genDefaultURI)

  def getCdnUrl(filePath : String) = "http://static.feedle.kr/" + filePath
  def getNewURL(pid : Int) = "http://www.feedle.kr/content/" + pid + "/"            /*   " http://www.feedle.kr/content/{pid}/ "  */
  def getNewURL(pid : Int, fileName : String) :String = getNewURL(pid) + fileName   /*   " http://www.feedle.kr/content/{pid}/{filename} "  */
  def getNewURL(pid : Int, filePath : File) : String = getNewURL(pid) + filePath.getName

  /* fileName for wpt (pid = 0)  projectUrl뒤에 /는 처리하지 않음. 어차피 localFile 이므로 문제는 없을 것으로 예상됨
     " ~/tmp/{pid}/{projectUrl(delete http:// or https://)}/runTest.{location.abbreviation}.xml "
     " ~/tmp/{pid}/{projectUrl(delete http:// or https://)}/xmlResult.{location.abbreviation}.xml "
     " ~/tmp/{pid}/{projectUrl(delete http:// or https://)}/image.{location.abbreviation}.xml "
     " ~/tmp/{pid}/{projectUrl(delete http:// or https://)}/FirstVideo.{location.abbreviation}.xml "
     " ~/tmp/{pid}/{projectUrl(delete http:// or https://)}/RepeatVideo.{location.abbreviation}.xml "
   */
  def getWPT_FirstUri(projectUrl : String, location : Location) = new File( defaultFileURI(WPTid) + deleteHTTP(projectUrl) + "/runTest"+location.abbreviation+".xml")
  def getWPT_XmlResultUri(projectUrl : String, location : Location) = new File( defaultFileURI(WPTid) + deleteHTTP(projectUrl) + "/xmlResult"+location.abbreviation+".xml")
  def getWPT_ImageUri(projectUrl:String, location:Location) = new File( defaultFileURI(WPTid) + deleteHTTP(projectUrl) + "/image"+location.abbreviation+".jpg")
  def getWPT_Video1Uri(projectUrl:String, location:Location) = new File( defaultFileURI(WPTid) + deleteHTTP(projectUrl) + "/FirstVideo"+location.abbreviation+".mp4")
  def getWPT_Video2Uri(projectUrl:String, location:Location) = new File(defaultFileURI(WPTid) + deleteHTTP(projectUrl) + "/RepeatVideo"+location.abbreviation+".mp4")


  /* 주의해서 사용할 것. 쿼리가 없는 곳에 사용 하길 권장함 */
  def deleteHTTP(urlString: String) : String = if (urlString.contains("http"))  urlString.split("//")(1) else urlString
  private def deleteQuery(urlString : String) : String = urlString.split('?')(0)

  //get Project Path
  def getFile (url : URL, pid : Int) : File = {
    def getPath(url : URL) : String =
      if ("/".equals(url.getPath) || "".equals(url.getPath)) url.getHost + "/index.html" else url.getHost + url.getPath

    val newFilePath = defaultFileURI(pid) + getPath(url) + (if(url.getQuery==null) "" else "@" + url.getQuery)    //query를 남김.
    new File(newFilePath)
  }

  //get Gen File Path

  val PNGSuffix = ".optimized.png"
  def getPNGOptimizedName(file : String) : String = {
    file + PNGSuffix
  }
  val JPGSuffix = ".compressed.jpg"
  def getJPGCompressedName(file : String) : String = {
    file + JPGSuffix
  }

  def getMinName(url : String) : String = {
    val dot_index = url.lastIndexOf('.')
    val slash_index = url.lastIndexOf('/')

    url.substring(slash_index+1, dot_index) +
      ".min." +
      url.substring(dot_index+1, url.length)
  }

  private def extension(contentType : ContentType) = contentType match {
    case ContentType.CSS => ".css"
    case ContentType.JS  => ".js"
    case _ => throw new IllegalArgumentException
  }

  def createMergeName(md5:String, contentType : ContentType) : String = {

    "merge_" + System.currentTimeMillis() + extension(contentType)
  }

  def createReplaceName(contentType : ContentType) : String = {

    "replace_" + System.currentTimeMillis + extension(contentType)
  }

  def createSpriteName() : String = {

    "sprite_" + System.currentTimeMillis + ".png"
  }

  //wpt 외에는 사용하지 않기를 권장함
  def isInWPTDIR(file : File) : Boolean = {

    file.toString.split("tmp/")(1).split('/')(0).toInt == 0
  }
}
