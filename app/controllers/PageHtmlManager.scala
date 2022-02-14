package controllers

import java.net.URL

import com.feedle.feo.optimize.OptimizeInvoker
import com.feedle.feo.util.Util
import com.feedle.feo.weboject.{HtmlObject, ObjectFactory}
import models.Page

import scala.collection.mutable
import scala.concurrent.{Future, Promise}



object PageHtmlManager {
  private class promiseHoler[T](val fn:Option[HtmlObject]=>T,val promise:Promise[T]){
    def apply(html:Option[HtmlObject]){
      try {
        promise success fn(html)
      }
      catch{case t:Throwable=>t.printStackTrace();promise failure t}
    }
  }

  private val reQ = new mutable.HashMap[Int,mutable.SynchronizedQueue[promiseHoler[_<:Any]]]
    with mutable.SynchronizedMap[Int,mutable.SynchronizedQueue[promiseHoler[_<:Any]]]
  private val PageDomHash = new mutable.HashMap[Int,HtmlObject] with mutable.SynchronizedMap[Int,HtmlObject]

  private def emptyQProb(page_id:Int)={val newQ = new mutable.SynchronizedQueue[promiseHoler[_<:Any]];reQ += page_id->newQ ; newQ}

  def isIdle(page_id:Int) = this.reQ.getOrElse(page_id,emptyQProb(page_id)).isEmpty

  def withHtmlObj[T](page_id:Int)(fn:Option[HtmlObject]=>T):Future[T]={
    val holder = new promiseHoler[T](fn,Promise.apply[T]())
    if(isIdle(page_id)){
      reQ.get(page_id).get enqueue holder
      holder(PageDomHash.get(page_id))
      fireNextFunc(page_id)
    }
    else
      reQ.get(page_id).get enqueue holder

    holder.promise.future
  }

  private def fireNextFunc(page_id:Int){
    val Q=reQ.get(page_id).get
    Q.dequeue()
    if(!isIdle(page_id)) {
      val holder = Q.front
      holder(PageDomHash.get(page_id))
      fireNextFunc(page_id)
    }
  }

  def refreshPage(page:Page,url:String){
    makeNewPage(page,url)
    reoptimizePage(page)
  }

  def makeNewPage(page:Page,host:String)=withHtmlObj(page.pageId){oldHtml=>
    val url = Util.AbsoluteURL(new URL("http://"+host),page.path)//+{if(page.query.isEmpty) "" else "?"+page.query})
    val html = ObjectFactory.createHtmlObject(url,page.header.getOrElse(""),page.projectId)
    html.SpritableImages
    PageDomHash+=page.pageId->html
  }

  def reoptimizePage(page:Page)=withHtmlObj(page.pageId){html=>
    OptimizeInvoker.reoptimize(page.projectId,html.get)
    PageDomHash+=page.pageId->html.get
  }

}
