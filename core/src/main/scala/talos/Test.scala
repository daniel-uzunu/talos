package talos

//import Macros._

object Test {

  def main(args: Array[String]): Unit = {
    println("test")

    //assert(4 > 3, "aaa")
  }

  /*case class Person(firstName: String, lastName: String, email: String)

  case class Book(title: String, author: Person, year: Int, isbn: String)

  // assumption: a person should always be valid, the context can't modify the validation rules for a person

  // TODO: naming

  trait Constraint[A]

  abstract class Constraints {
    def constraints: List[Constraint[_]]

    def constraint[T](fn: T => Boolean): Constraint[T] = ??? // macro
  }

  implicit object MyConstraints extends Constraints {
    override lazy val constraints = List(personConstraint, bookConstraint)

    val personConstraint = constraint[Person] {
      p => p.firstName != "" && p.lastName != ""
    }

    val bookConstraint = constraint[Book] { b =>
      b.title != "" && b.year > 2000
    }
  }*/

  // def validate[A](obj: A)(implicit constraints: Constraints): Result
}
