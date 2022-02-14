package controllers

import com.feedle.proxy.{ProxyCommand, ProxyCommander, ProxyXml}
import play.api.mvc._



object ProxyDeployController extends Controller{
  def deployAll(req_project_id : Int) = Action { rs =>
    if (!SessionManager.checkProject(rs.session, req_project_id))
      Forbidden("does not have permission.")

    val xml = new ProxyXml(req_project_id).toXml

    val command = ProxyCommand.DEPLOY_PROXY_CONF
    ProxyCommander.commandASIA(command, xml.toString())
    ProxyCommander.commandEU(command, xml.toString())
    ProxyCommander.commandKOREA(command, xml.toString())
    ProxyCommander.commandUSA(command, xml.toString())

    Ok
  }
}
