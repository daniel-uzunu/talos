import scala.language.implicitConversions

trait Constraint[A] {
  def and(other: Constraint[A]): Constraint[A] = ???
  def or(other: Constraint[A]): Constraint[A] = ???
}

trait DefaultConstraints {
  def constraint[T](c: Constraint[T]): Constraint[T] = ???

  def isValid[T](obj: T)(implicit constraint: Constraint[T]): Constraint[T] = ???
  def isValid[T](obj: Option[T])(implicit constraint: Constraint[T]): Constraint[T] = ???
  def isEmail[T](s: String): Constraint[T] = ???

  implicit def toConstraint[T](expr: Boolean): Constraint[T] = ???

  def not[T](constraint: Constraint[T]): Constraint[T] = ???
}

object DefaultConstraints extends DefaultConstraints

case class User(name: String, givenName: String, familyName: String, email: String, isMarried: Boolean)

object MyConstraints extends DefaultConstraints {
  val a: User = null
  import a._

  implicit val userConstraint = constraint[User] {
    (name != null or givenName.length > 5) and
      isEmail(email) and
      s"$givenName, $familyName".contains("ab") and
      isMarried and
      familyName == ""
  }

  implicit val userConstraint1 = constraint[User] {
    name != "" and givenName != "" and familyName != "" and email != "" and
    isEmail(email) and name.matches("""^[a-zA-Z][a-zA-Z0-9_\-\.]*$""") and
    isMarried
  }
}
