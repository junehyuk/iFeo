package com.feedle.feo.weboject

import java.net.URL

import com.feedle.feo.optimize.csssprite.{ImageWithMargin, SpriteableImageCrawler}
import com.feedle.feo.weboject.HtmlObject.CssDeclarationWithObject
import com.phloc.css.ECSSVersion
import com.phloc.css.decl.CSSDeclaration
import com.phloc.css.reader.CSSReaderDeclarationList
import models.WebContent
import org.jsoup.Jsoup
import org.jsoup.nodes._

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 *
 * Html Wrapper Class
 * HtmlObject는 jsoup라이브러리를 기반으로 Html을 파싱하여 사용한다.
 *
 */

object HtmlObject {
  /**
   * 새로운 HtmlObject를 생성
   * @param content 생성할 웹컨텐트
   * @return 새로운 HtmlObject
   */
  def apply(content: WebContent) = new HtmlObject(content)

  /**
   * CSS 수정을 위한 스타일 정의 정보를 묶은 케이스 클래스이다.
   * @param decl phloc-css의 Css정의문
   * @param url 해당 스타일 정의문을 로드한 url
   * @param addFunc 정의문 뒤에 새로운 정의문을 추가하는 함수
   * @param setChanged 정의문이 변경되었을때 변경됨을 체크
   */
  case class CssDeclarationWithObject(decl: CSSDeclaration, url: URL, addFunc: CSSDeclaration => Unit, setChanged: () => Unit)
}

class HtmlObject(val webContent: WebContent) extends WebObject {

  /**
   * CssObject와 해당 Object를 불러오는 Html Element를 매핑해놓은 맵
   */
  val cssHash = mutable.HashMap[Element, CssObject]()
  /**
   * JsObject와 해당 Object를 불러오는 Html Element를 매핑해놓은 맵
   */
  val jsHash = mutable.HashMap[Element, JsObject]()

  /**
   * inlineCss리스트
   */
  val inlineStyleList = mutable.Buffer[InlineCssObject]()
  /**
   * jsoup으로 파싱한 Html Dom Tree
   */
  val domTree = Jsoup.parse(webContent.body.mkString)

  /**
   * Html내의 모든 imgTag
   */
  val imgTagList: mutable.Buffer[Element] = domTree.getElementsByTag("img")

  //초기화 탐색
  initialTravel(domTree)

  /**
   * spritable 이미지 연산결과 margin프리셋
   * object_id->image with margin
   */
  lazy val SpritableImages ={
    val map= mutable.HashMap[Int, ImageWithMargin]()
    SpriteableImageCrawler.crawlSpritableImage(this).foreach(x => map += x.content.objectId -> x)
    map
  }


  var recentStyleHash : mutable.HashMap[Node, mutable.Buffer[CssDeclarationWithObject]] = null

  /**
   * StyleHash를 연산.
   * 하위 모든 Css를 모두 합침.
   * @return StyleHash
   */
  def styleHash:mutable.HashMap[Node, mutable.Buffer[CssDeclarationWithObject]] = {
    if (recentStyleHash == null)
    {
      recentStyleHash = mutable.HashMap[Node, mutable.Buffer[CssDeclarationWithObject]]()

      cssHash.flatMap(_._2.getAllStyleRules).foreach{rule =>
        val selector = rule._1.getAllSelectors

        val cssUrl = rule._2 match {
          case external: ExternalCssObject => external.webContent.originUrl
          case _ => webContent.originUrl
        }

        val declBuffer = rule._1.getAllDeclarations.map {
          //declaration을 오브젝트 정보와 함께 묶음.
          x => new CssDeclarationWithObject(x, cssUrl, rule._2.addStyle(rule._1), rule._2.setChanged)
        }

        selector.foreach {
          selec =>
            //셀렉터 하나하나마다 수행함.
            val selectorStr = CssObject.CssWriter.getCSSAsString(selec)

            //todo Issue:  :hover 등 세분화 해서 분류해야함.!!
            if (!selectorStr.contains(":"))
              domTree.select(selectorStr).foreach {
                element =>
                  //모든 셀렉터에 해당하는 돔엘리먼트에 정의문을 추가해줌.
                  addStyleRule(element, declBuffer)
              }
        }
      }

      //inlineStyle를 추가함.
      //inline은 가장 우선순위가 높아서 마지막에 추가.
      inlineStyleList.foreach{inline=>
        addStyleRule(inline.element,inline.styleList
            .map(x => new CssDeclarationWithObject(x, inline.originURL, inline.addStyle, inline.setChanged))
        )
      }
    }

    //recentStyleRule에 추가함.
    def addStyleRule(element: Element, rule: mutable.Buffer[CssDeclarationWithObject]) {
      val styles = recentStyleHash getOrElse (element,
        {val newBuf = mutable.Buffer[CssDeclarationWithObject]()
        recentStyleHash += element -> newBuf
        newBuf}
      )
      styles ++= rule
    }

    recentStyleHash
  }

  def initialTravel(node: Node): Unit = node match {
    case element: Element =>
      element.tagName match {
        case "script" =>                                                  //script tag
          val src = element.attr("src")

          if (src.equals("")) {
            if (node.childNodes.size() > 0)
              node.childNode(0) match {
                case dataNode: DataNode =>
                  addInternalJs(element, dataNode)
                case _ =>
                  System.err.println("no DataNode in link Node")
              }
          }
          else {
            addExternalJs(element, src)
          }
        case "style" if element.attr("language").equals("javascript") =>  //javascript가있는 style태그
          val src = element.attr("src")
          if (src.equals("")) {
            if (node.childNodes.size() > 0)
              node.childNode(0) match {
                case dataNode: DataNode =>
                  addInternalJs(element, dataNode)
                case _ =>
                  System.err.println("no DataNode in link Node")
              }
          }
          else {
            addExternalJs(element, src)
          }
        case "link" if element.attr("rel").equals("stylesheet") =>        //stylesheet인 link
          val src = element.attr("href")
          if (src.equals("")) {
            if (node.childNodes.size() > 0)
              node.childNode(0) match {
                case dataNode: DataNode =>
                  addInternalCss(element, dataNode)
                case _ =>
                  System.err.println("no DataNode in link Node")
              }
          }
          else {                                                          
            addExternalCss(element, src)
          }
        case "style" =>                                                   //Style태그
          val src = element.attr("src")
          if (src.equals("")) {
            if (node.childNodes.size() > 0)
              node.childNode(0) match {
                case dataNode: DataNode =>
                  addInternalCss(element, dataNode)
                case _ =>
                  System.err.println("no DataNode in link Node")
              }
          }
          else {
            addExternalCss(element, src)
          }
        case _ =>                                                         //그외
          val inlineStyle = element.attr("style")
          if (!inlineStyle.equals(""))
            try {
              addInlineCss(element, CSSReaderDeclarationList
                .readFromString(inlineStyle, ECSSVersion.CSS30)
                .getAllDeclarations)
            }
            catch {
              case e: Exception =>
            }


          node.childNodes.foreach {
            x => initialTravel(x)
          }
      }

    case _ =>
      node.childNodes.foreach {
        x => initialTravel(x)
      }
  }

  /**
   * InternalCss를 생성하고 추가한다.
   * @param parent  Tag Element
   * @param content Internal Css의 내용
   */
  def addInternalCss(parent: Element, content: DataNode) {
    val cssobj = ObjectFactory.createInternalCssObject(content.getWholeData, webContent.originUrl,webContent.projectId)
    cssHash += parent -> cssobj
  }

  /**
   * ExternalCss를 생성하고 추가한다.
   * @param parent  Tag Element
   * @param url 새로운 Css의 URL
   */
  def addExternalCss(parent: Element, url: String) {
    val cssobj = ObjectFactory.createCssObject(url, webContent.originUrl,webContent.projectId)
    cssHash += parent -> cssobj
  }

  /**
   * Inline를 생성하고 추가한다.
   * @param parent  Tag Element
   * @param rule 파싱된 CSS Style Rule
   */
  def addInlineCss(parent: Element, rule: mutable.Buffer[CSSDeclaration]) {
    val inlineCss = new InlineCssObject(parent, rule, webContent.originUrl)
    inlineStyleList += inlineCss

  }

  /**
   * InternalJs를 생성하고 추가한다.
   * @param parent  Tag Element
   * @param content Internal Js의 내용
   */
  def addInternalJs(parent: Element, content: DataNode) {
    val jsobj = ObjectFactory.createInternalJsObject(content.getWholeData, webContent.originUrl,webContent.projectId)
    jsHash += parent -> jsobj
  }

  /**
   * ExternalJs를 생성하고 추가한다.
   * @param parent  Tag Element
   * @param url 새로운 Js의 URL
   */
  def addExternalJs(parent: Element, url: String) {
    val jsobj = ObjectFactory.createJsObject(url, webContent.originUrl,webContent.projectId)
    jsHash += parent -> jsobj
  }

}
