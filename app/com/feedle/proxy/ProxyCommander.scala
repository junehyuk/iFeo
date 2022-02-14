package com.feedle.proxy

import com.feedle.proxy.ProxyCommand.ProxyCommand
import com.feedle.proxy.ProxyCommandServer._
import controllers.TrafficController
import scala.collection.mutable
import scala.collection.immutable
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by infinitu on 2014. 6. 14..
 */

object ProxyCommand extends Enumeration{
  type ProxyCommand = this.Val
  val HEARTBEAT                       = new Val(0x00)
  val DEPLOY_PROXY_CONF               = new Val(0x11)
  val STOP_PROXY                      = new Val(0x12)
  val STOP_ALL_PROXY                  = new Val(0x13)
  val GET_ALL_PROXY                   = new Val(0x14)
  val GET_PROXY_TRAFFIC               = new Val(0x21)
  val GET_PROXY_TRAFFIC_AND_RESET     = new Val(0x22)
  val RESET_TRAFFIC                   = new Val(0x23)
  val GET_ALL_PROXY_TRAFFIC           = new Val(0x24)
  val GET_ALL_PROXY_TRAFFIC_AND_RESET = new Val(0x25)
  val RESET_ALL_TRAFFIC               = new Val(0x26)
}

object ProxyCommander {

  case class CommandHolder(proxyId:Int,command:ProxyCommand,content:String,promise:Promise[String])

  val ProxyServer = new ProxyCommandServer(new ResponseReceiver {

    override protected def active(proxyId: Int){
      ProxyCommander.proxyActive(proxyId)
    }

    override protected def receive(proxyId: Int, resCode: Int, content: String){
      ProxyCommander.receive(proxyId,resCode,content)
    }
  },
  new TrafficCntCycleHandler {
    override protected def tick(proxyId: Int){
      command(proxyId,ProxyCommand.GET_ALL_PROXY_TRAFFIC_AND_RESET,"") onSuccess{case json:String=>TrafficController.addTraffic(json,proxyId)}
    }
  })

  val ProxyCommandQueue = immutable.Map(
    FEEDLE_PROXY_ASIA_ID  -> new mutable.SynchronizedQueue[CommandHolder],
    FEEDLE_PROXY_EU_ID    -> new mutable.SynchronizedQueue[CommandHolder],
    FEEDLE_PROXY_USA_ID   -> new mutable.SynchronizedQueue[CommandHolder],
    FEEDLE_PROXY_KOREA_ID -> new mutable.SynchronizedQueue[CommandHolder]
  )

  private def proxyActive(proxyId:Int){
    ProxyCommandQueue.get(proxyId).get
      .foreach(holder=> ProxyServer.sendCommand(proxyId, holder.command.id, holder.content))
  }

  private def receive(proxyId:Int, resCode:Int, content:String){
    val holder = ProxyCommandQueue.get(proxyId).get dequeue()
    resCode match{
      case 0x10=> //OK
        holder.promise success content
      case 0x20=> //Error
        holder.promise failure new Throwable(content)
      case 0x30=> //not ready
        holder.promise failure new Throwable("Not Ready")
    }
  }

  private def command(proxyId:Int,command:ProxyCommand,content:String):Future[String]={
    val promise = Promise[String]()
    val holder = new CommandHolder(proxyId,command,content,promise)
    ProxyCommandQueue.get(proxyId).get enqueue holder
    ProxyServer.sendCommand(proxyId,command.id,content)
    promise.future
  }

  def commandASIA (cmd:ProxyCommand,content:String) = command(FEEDLE_PROXY_ASIA_ID, cmd,content)
  def commandEU   (cmd:ProxyCommand,content:String) = command(FEEDLE_PROXY_EU_ID,   cmd,content)
  def commandUSA  (cmd:ProxyCommand,content:String) = command(FEEDLE_PROXY_USA_ID,  cmd,content)
  def commandKOREA(cmd:ProxyCommand,content:String) = command(FEEDLE_PROXY_KOREA_ID,cmd,content)

}
