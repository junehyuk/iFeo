package controllers

import play.api._
import play.api.mvc._
import com.feedle.feo.wpt.WPTConnector

object ApplicationJH extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  def pagetest = Action {
   // val aaa = WPTConnector("www.naver.com")
    Tool.downloadVideo("140618_VW_K0E")

    Ok("dd")
  }

  def listproject = Action {
    Ok(Tool.listProject(10).toString)
  }

}