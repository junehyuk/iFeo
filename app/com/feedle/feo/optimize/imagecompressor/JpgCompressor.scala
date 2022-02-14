package com.feedle.feo.optimize.imagecompressor

import java.awt.image.BufferedImage
import java.io._
import java.util.{Iterator => JIterator}
import javax.imageio.stream.ImageOutputStream
import javax.imageio.{IIOImage, ImageIO, ImageWriteParam, ImageWriter}

import com.feedle.feo.content.ObjectManager
import com.feedle.feo.util.PathManager
import controllers.{DBController => DB}
import models._

/**
 * compressionLevel where 0 is no compression and 9 is the highest available compression
 */
class JpgCompressor(val pid : Int, val objs : Seq[WebContent], val compressionLevel : Int = 5) {
  if ( compressionLevel > 9 || compressionLevel < 0 )
    throw new IllegalArgumentException("compressionLevel where 0 is no compression and 9 is the highest available compression")

  val dir = PathManager.getGenURI(pid)
  val origin_dest : Seq[(WebContent, WebContent)] = objs.map{x=> x -> ObjectManager(PathManager.getJPGCompressedName(x.localFileName), x.projectId).get}.toSeq

  // start
  origin_dest.map{x => x._1.localFile -> x._2.localFile}.foreach(compress)


  def compress(map : (File, File)) = {

    val imageFile : File = map._1
    val compressedImageFile : File = map._2
    compressedImageFile.getParentFile.mkdirs()

    val is : InputStream = new FileInputStream(imageFile)
    val os : OutputStream = new FileOutputStream(compressedImageFile)

    val quality : Float = 0.1f * compressionLevel

    // create a BufferedImage as the result of decoding the supplied InputStream
    val image : BufferedImage = ImageIO.read(is)

    // get all image writers for JPG format
    val writers : JIterator[ImageWriter] = ImageIO.getImageWritersByFormatName("jpg")

    if (!writers.hasNext)
      throw new IllegalStateException("No writers found")

    val writer : ImageWriter = writers.next()
    val ios : ImageOutputStream = ImageIO.createImageOutputStream(os)
    writer.setOutput(ios)

    val param : ImageWriteParam = writer.getDefaultWriteParam

    // compress to a given quality
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
    param.setCompressionQuality(quality)

    // appends a complete image stream containing a single image and
    //associated stream and image metadata and thumbnails to the output
    writer.write(null, new IIOImage(image, null, null), param)

    // close all streams
    is.close()
    os.close()
    ios.close()
    writer.dispose()
  }


}
