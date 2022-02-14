import com.feedle.proxy.ProxyCommander
import play.api.mvc.{Handler, RequestHeader}
import play.api.{Application, GlobalSettings}
import scala.concurrent._
import ExecutionContext.Implicits.global
import controllers.PageHtmlManager
import models._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._


/**
 * Created by infinitu on 2014. 6. 9..
 */


object Global extends GlobalSettings{

  val ProxyServer = ProxyCommander.ProxyServer

  override def beforeStart(app: Application): Unit = {
    ProxyServer.run()
    super.beforeStart(app)
  }

  override def onStart(app: Application) = {
    future{

      DB.withSession{implicit session =>
        //TEST용 val toLoadProject = Seq(1)
        val toLoadProject =Projects.tQuery.map(_.projectId).list
        val toLoadPage = (for{
          (page,project) <- Pages.tQuery.filter(_.projectId inSet toLoadProject) innerJoin Projects.tQuery on (_.projectId === _.projectId)
        } yield(project.projectId,project.domain,page)).list()
        toLoadPage.par.foreach(prjectAndPage=>PageHtmlManager.refreshPage(prjectAndPage._3,prjectAndPage._2))
      }


    }

    super.onStart(app)
  }

  override def onStop(app: Application): Unit = {
    ProxyServer.stop()
    super.onStop(app)
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    if(request.host equals "simulation.feedle.kr")
      Some(controllers.Simulation.index)
    else
      super.onRouteRequest(request)
  }
}
