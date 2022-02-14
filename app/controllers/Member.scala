package controllers

import play.api._
import play.api.mvc._
import models._
import play.api.libs.json._
import scala.io.Source


object Member extends Controller {


  def logout = Action {
    Redirect(routes.Application.index()).withNewSession
  }


  //////////////////////////////////////////////////////
  //      회원가입 부분
  //////////////////////////////////////////////////////

  def register = Action {
    Ok(views.html.member.signup())
  }

  def registerUpdate = Action { request =>

    val user_id = request.body.asFormUrlEncoded.get("id")(0)
    val user_pwd = request.body.asFormUrlEncoded.get("pwd")(0)
    val email = request.body.asFormUrlEncoded.get("email")(0)

    val userInfo:User = new User(0,user_id,user_pwd,email)
    val result = DBController.join(userInfo)

    val registerResult: JsResult[String] = (result \ "result").validate[String]

    registerResult match {

      case s: JsSuccess[String] => {
        Ok("Ok")
      }
      case e: JsError => {
        (result \ "error").as[String] match  {
          case "Email Exists" => Ok("이미 존재하는 이메일입니다.")
          case "Id Exists" =>  Ok("이미 존재하는 아이디입니다.")
          case _ => Ok( (result \ "error").as[String] )
        }
      }

    }
  }


  //////////////////////////////////////////////////////
  //      회원정보수정 부분
  //////////////////////////////////////////////////////

  def modify = Action { request =>

    if (SessionManager.checkUser(request.session)) {
      val userData = DBController.getMember(request.session.get("user").get)
      Ok(views.html.member.modify(userData)(request))
    } else {
      Redirect(routes.Member.login())
    }

  }

  def modifyUpdate = Action { request =>

      if(!SessionManager.checkUser(request.session))
        Ok("권한이 없습니다.")
      else {
        val user_id = request.session.get("user").get
        val user_pwd = request.body.asFormUrlEncoded.get("pwd")(0)
        val email = request.body.asFormUrlEncoded.get("email")(0)

        val userInfo:User = new User(SessionManager.getUserId(user_id),user_id,user_pwd,email)
        val result = DBController.modify(userInfo)
        Ok("Ok")
      }

  }


//////////////////////////////////////////////////////
//      회원로그인 부분
//////////////////////////////////////////////////////

  def login = Action {
    Ok(views.html.member.login())
  }

  def loginCheck() = Action { request =>

    val user_id = request.body.asFormUrlEncoded.get("id")(0)
    val user_pwd = request.body.asFormUrlEncoded.get("pwd")(0)

    val result = DBController.login(user_id,user_pwd)
    val loginResult: JsResult[String] = (result \ "result").validate[String]

    loginResult match {
      case s: JsSuccess[String] => Redirect(routes.Tool.index).withSession("user"->user_id)
      case e: JsError => {
        Ok(views.html.alertError("아이디 또는 비밀번호가 틀렸습니다."))
      }
    }

  }



}