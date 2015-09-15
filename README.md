# talos

Talos is a Scala validation library in which the validation rules are stored as data structures. The advantage of this approach is that the validation rules can be read and converted to another format. An example, would be the typical client-server scenario: the validation rules can be encoded as JSON and sent to the client.

Example:

```scala
trait DefaultConstraints {
  def constraint[T](fn: T => Constraint[T]): Constraint[T] = ???

  def isValid[T](obj: T)(implicit constraint: Constraint[T]): Constraint[T] = ???
  def isValid[T](obj: Option[T])(implicit constraint: Constraint[T]): Constraint[T] = ???
  def isEmail(s: String): Constraint[T] = ???

  implicit def toConstraint(expr: Boolean): Constraint[T] = ???
}

object DefaultConstraints extends DefaultConstraints
```

```scala
case class StringMember[T](name: String, clazz: Class[T]) extends Member[T, String](name, clazz)

case class NotEmptyConstraint[T](member: StringMember[T]) extends Constraint[T]
```

```scala
case class User(name: String, givenName: String, familyName: String, email: String, isMarried: Boolean)
case class BlogPost(title: String, author: User)

object MyConstraints extends DefaultConstraints {
  implicit val userConstraint = constraint[User] { user =>
    user.name != "" && isEmail(user.email)
  }

  implicit val blogPostConstraint = constraint[BlogPost] { post =>
    post.title != "" && isValid(post.author)
  }
}
```

```scala
trait Constraint[A]

case class NonEmptyConstraint[A] (member: Member[String]) extends Constraint[A]

member != ""

case class User(name: String, givenName: String, familyName: String, email: String, isMarried: Boolean)

case class BlogPost(title: String, author: User)

abstract class SomeConstraint[A] extends Constraint[A] {
  abstract val a: Constraint[A]
  abstract val model: A
}

val userConstraint = NonEmptyConstraint[User](Member("email"))

val userConstraint = SomeConstraint[User](u => u.name != "" && u.isMarried && u.email != "")

val blogPostConstraint = SomeConstraint[BlogPost](p => p.title != "" && p.author respects userConstraint)

val userConstraint = m(_.name) != "" && m(_.email) != ""

// should I use implicits?
/*implicit*/ object UserConstraint extends SomeConstraint[User] {
  override val constraint = m(_.name) != "" && m(_.email) != ""
}

trait SomeConstraint[B] {
  abstract val constraint: Constraint[B]
  def m(fn: (obj: B) => String): StringMember[B] = macro ???
  def m[A](fn: (obj: B) => A): Member[B] = macro ???
}
```
