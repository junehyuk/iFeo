package com.feedle.feo.optimize.imagecompressor

import com.feedle.feo.content.ObjectManager
import com.feedle.feo.util.PathManager
import controllers.{DBController => DB}
import models.WebContent

/**
 * compressionLevel where 0 is no compression and 9 is the highest available compression
 */
class Pngtastic(val pid: Int, val objs : Seq[WebContent], val compressionLevel : Int = 5) {
  if ( compressionLevel > 9 || compressionLevel < 0 )
    throw new IllegalArgumentException("compressionLevel where 0 is no compression and 9 is the highest available compression")

  /*
      private final val HELP: String = "java -jar pngtastic-x.x.jar com.googlecode.pngtastic.PngtasticOptimizer [options] file1 [file2 ..]\n" + "Options:\n" +
      "  --toDir            the directory where optimized files go (will be created if it doesn't exist)\n" +
      "  --fileSuffix       string appended to the optimized files (file.png can become file.png.optimized.png)\n" +
      "  --removeGamma      remove gamma correction info if found\n" +
      "  --compressionLevel the compression level; 0-9 allowed (default is to try them all by brute force)\n" +
      "  --compressor       path to an alternate compressor (e.g. zopfli)\n" +
      "  --logLevel         the level of logging output (none, debug, info, or error)\n"
  */
  val dir = PathManager.getGenURI(pid)
  val origin_dest : Seq[(WebContent, WebContent)] = objs.map{x=> x -> ObjectManager(PathManager.getPNGOptimizedName(x.localFileName), x.projectId).get}.toSeq

  val filePaths = origin_dest.map{x=>x._1.localPath}

  val options : Array[String] = Array(
    "--toDir", dir,
    "--removeGamma",
    "--compressionLevel", compressionLevel.toString,
    "--fileSuffix", PathManager.PNGSuffix
  )

  com.googlecode.pngtastic.PngtasticOptimizer.main(
    options ++ filePaths
  )
}
