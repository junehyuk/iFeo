package controllers

import java.io.File
import java.net.URL

import com.feedle.feo.content.ObjectManager
import com.feedle.feo.util.PathManager
import play.api.mvc._

import scala.slick.model.codegen.SourceCodeGenerator

object ApplicationYJ extends Controller {

  def codegen = Action {
    SourceCodeGenerator.main(
      Array(
        "scala.slick.driver.MySQLDriver",
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://feedle.kr:3306/feedleFEO_test" ,
        "tmp/",
        "codegen/",
        "infinitu",
        "dhwlddj1004"
      )
    )
    Ok("code gen")
  }

  def getContent(pid:Int, filename:String) = Action {
    val inline = true
    val content = PathManager.getGenFilePath(pid, filename)
    Ok.sendFile(new File(content), inline)
  }
  def  index = Action {

    val start = System.currentTimeMillis()
    val body =
      """{"item":[{"object_id":13801},{"object_id":13802},{"object_id":13803}]}"""
    val result =ObjectManager(new URL("http://hyundaicard.com/favicon.ico"), 1)

    Ok(result.toString)
    //      Ok ("done in " + (System.currentTimeMillis() - start) +"\ntest merger" + mergObjs )
  }

  def test(objId : Int) = Action {

    Ok("result : " )

  }
}