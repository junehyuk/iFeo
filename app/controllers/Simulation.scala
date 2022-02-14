package controllers

import com.feedle.feo.util.Util
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import  scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by infinitu on 2014. 6. 9..
 */
object Simulation extends Controller{
  val feedleQueryPattern = "feedle_simulation=([0-9]+)".r
  def index = Action.async{ rs =>

    val query = rs.getQueryString("feedle_simulation")
    if(query.isDefined)
    {
      PageHtmlManager.withHtmlObj(query.get.toInt) {html=>
        if (html.isEmpty)
          NotFound("not found")
        else
          Ok(html.get.domTree.toString).as("text/html;charset=UTF-8")
      }
    }
    else
    {
      val simId = feedleQueryPattern.findFirstMatchIn(rs.headers.get("Referer").getOrElse(""))
      if(simId.isDefined)
      {
        PageHtmlManager.withHtmlObj(simId.get.group(1).toInt) { html =>
          if (html.isEmpty)
            NotFound("not found")
          else
            MovedPermanently(Util.AbsoluteURL(html.get.webContent.originUrl, rs.uri).toString)
        }
      }
      else
        Future{NotFound("not found")}
    }
  }
}
