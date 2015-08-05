# talos

Talos is a Scala validation library in which the validation rules are stored as data structures. The advantage of this approach is that the validation rules can be read and converted to another format. An example, would be the typical client-server scenario: the validation rules can be encoded as JSON and sent to the client.

Example:

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
  def m(fn: (obj: B) => String): Member[B] = macro ???
  def m[A](fn: (obj: B) => A): Member[B] = macro ???
}
```
