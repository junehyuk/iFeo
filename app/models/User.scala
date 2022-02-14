package models

import play.api.db.slick.Config.driver.simple._
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
import scala.slick.jdbc.{GetResult => GR}

/** Entity class storing rows of table Users
  *  @param userId Database column user_id AutoInc, PrimaryKey
  *  @param id Database column id
  *  @param pwd Database column pwd  */
case class User(userId: Int, id: String, pwd: String, email:String)
case class UserWithId(id:String, pwd:String)
case class UserWithEmail(email:String, pwd:String)
object Users{

  /** GetResult implicit for fetching UsersRow objects using plain SQL queries */
  implicit def GetResultUser(implicit e0: GR[Int], e1: GR[String]): GR[User] = GR{
    prs => import prs._
      User.tupled((<<[Int], <<[String], <<[String], <<[String]))
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
  val tQuery = TableQuery[Users]
}
/** Table description of table users. Objects of this class serve as prototypes for rows in queries. */
class Users(tag: Tag) extends Table[User](tag, "users") {
  def * = (userId, id, pwd, email) <> (User.tupled, User.unapply)
  /** Maps whole row to an option. Useful for outer joins. */
  def ? = (userId.?, id.?, pwd.?, email.?).shaped.<>({r=>import r._; _1.map(_=> User.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

  /** Database column user_id AutoInc, PrimaryKey */
  val userId: Column[Int] = column[Int]("user_id", O.AutoInc, O.PrimaryKey)
  /** Database column id  */
  val id: Column[String] = column[String]("id")
  /** Database column pwd  */
  val pwd: Column[String] = column[String]("pwd")
  /** Database column email  */
  val email: Column[String] = column[String]("email")
}


case class UserProfile(
country: String,
address: Option[String],
age: Option[Int]
)