package com.feedle.feo.optimize.ftpuploader


import com.feedle.feo.util.{ContentType, PathManager}
import models.WebContent
import org.apache.commons.net.ftp.{FTP, FTPClient}

//
case class FTPMapper(pid : Int, x : WebContent) {
  val url = x.originUrl
  val filePathBeforeName = {
    val path = x.localPath.split(PathManager.defaultFileURI(pid))(1)
    path.substring(0, path.lastIndexOf('/')+1)
  }
  val fileName = x.localFileName
  val localPath = x.localPath
  val originUrl = url.toString
  val cdnFilePidDir : String = pid + "/"
  val cdnFilePath = cdnFilePidDir+filePathBeforeName+fileName
  val cdnUrl = PathManager.getCdnUrl(cdnFilePath)
}
class FtpUploader {
  val f_host = "feedle.upload.akamai.com"
  val f_id = "feedle"
  val f_password = "FeedleSoma!1"
  val defaultDir = "151617/"
  val client = new FTPClient()


  def listFile(dir : String) = client.listDirectories(dir)

  def downloadAndUpload( mapper : FTPMapper ) : Boolean = {

    try {
      client.connect(f_host)
      client.login(f_id, f_password)

      client.cwd(defaultDir)

      val dir = (mapper.cdnFilePidDir + mapper.filePathBeforeName).split('/')

      dir.foreach { x=>
        if (client.cwd(x) == 550 ) client.mkd(x); client.cwd(x)
      }

      if ( mapper.x.contentType == ContentType.IMAGE) {
        client.setFileType(FTP.BINARY_FILE_TYPE)
        client.setFileTransferMode(FTP.BINARY_FILE_TYPE)
      }
      val result = client.storeFile(mapper.fileName, mapper.x.bodyInputStream)

      client.disconnect()

      result
    } catch {
      case ex : Exception => println("cant store"); false
    }
    true
  }
}