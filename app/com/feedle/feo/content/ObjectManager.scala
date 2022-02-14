package com.feedle.feo.content

import java.io.File
import java.net.URL

import models.{WebContent, RequestHeader}

object ObjectManager {

  //urls with no requestHeader
  def apply(requestUrls : Seq[URL], pid : Int) : Seq[Option[WebContent]]= new WebContentInsertHolder().get(requestUrls, pid)

  //normal
  def apply(requestUrl : URL, headerMap : RequestHeader=new RequestHeader, pid : Int) : Option[WebContent] = new WebContentInsertHolder().get(requestUrl, headerMap, pid)
  def apply(requestUrl : URL, pid : Int) : Option[WebContent] = new WebContentInsertHolder().get(requestUrl, new RequestHeader, pid)

  //gen
  def apply(fileName : String, pid : Int ) : Option[WebContent]= new WebContentInsertHolder().gen(fileName, pid )
//  def apply(fileName : Seq[File], pid : Int ) = new WebContentInsertHolder().gen(fileNames, pid )
  /* 이거 만드려다가 계속 실패하는 이유가 db가 존재하는 지 / 존재하지 않는지 list를 불러오는게 그냥 존재하는 것만 오기 때문에 어떤게 있고 없는 지 구분이 어려움 (diff/intersect 등을 쓰면 해결 가능 할지도 으으근데 머리쓰는건 좀 미뤄두자)
   일단 genfile을 만든다는 거는 cdn/ minify/ merge/ css_sprite/ compressor 중 하나인데
     minify와 compress의 경우는 어차피 결과물이 어떻게 되든 똑같음 (PathManager의 경로가 같)
     merge와 css_sprite의 경우에는 system time으로 이름을 결정하므로 같을 수가 없다 그러므로 여기서 문제가 생길 이유는 없음
     cdn은 아직 진행하지 않아서 어떤 문제가 발생할지 잘 모르겠음

     문제가 되는경우는 일단 minify랑 compress의 경우. 이 경우에는 새로 만들 필요 없이 db에 있는 값만 리턴해주면됨
     시간이 많으면 만들 수 도 있을 것 같다^^;
     일단 천천히 돌려야징(minify와 compress에서 시간이 많이 걸림 1:1매칭이므로)
  */


//  def gen(files : Seq[File], pid : Int ) = new WebContentInsertHolder().gens(files, pid )

  //WPT 외에는 사용하지 말 것
  def wpt(file : File, requestUrl : String) = new WebContentInsertHolder().wptRunTest(file, requestUrl)
}