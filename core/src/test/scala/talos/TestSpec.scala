package talos

import org.scalatest.{Matchers, FunSpec}

class TestSpec extends FunSpec with Matchers {
  case class Person(firstName: String, lastName: String, age: Int = 18)
  case class Post(title: String, author: Person)

  case class Account(currency: String, amount: Double)

  describe("Simple Validations") {

    object MyConstraints extends DefaultConstraints {
      implicit val personConstraint = constraint[Person] { p =>
        p.firstName != "" && p.lastName != "" && p.age > 0 && (p.age < 20 || p.age > 50)
      }

      implicit val postConstraint = constraint[Post] { p =>
        p.title != ""
      }
    }

    import MyConstraints._

    ignore("should generate a constraint") {
      personConstraint shouldEqual AndConstraint(
        NotEmptyConstraint[Person]("firstName"),
        NotEmptyConstraint[Person]("lastName"))
      postConstraint shouldEqual NotEmptyConstraint[Post]("title")
    }

    it("should check for empty strings") {
      validate(Person("John", "Doe")) shouldEqual Success
      validate(Person("", "Doe")) shouldEqual Failure
      validate(Person("John", "")) shouldEqual Failure
      validate(Person("John", "Doe", 30)) shouldEqual Failure
    }
  }

  describe("null checks") {}

  describe("String validations") {
    import DefaultConstraints._

    it("should check for empty strings") {
      implicit val c = constraint[Person](p => p.firstName != "")

      validate(Person("John", "Doe")) shouldEqual Success
      validate(Person("John", "")) shouldEqual Success
      validate(Person("", "Doe")) shouldEqual Failure
    }
  }

  describe("Numeric validations") {
    import DefaultConstraints._

    it("should enforce greater than constraints on numeric values") {
      implicit val c = constraint[Account](a => a.amount > 100.5)

      validate(Account("EUR", 450.8)) shouldEqual Success
      validate(Account("USD", 98.9)) shouldEqual Failure
    }

    it("should enforce greater than constraints on numeric values 2") {
      implicit val c = constraint[Account](a => a.amount > 100)

      validate(Account("EUR", 450.8)) shouldEqual Success
      validate(Account("USD", 98.9)) shouldEqual Failure
    }
  }

  describe("Composite validations") {

  }
}
