package talos

import org.scalatest.{Matchers, FunSpec}
import ConstraintsValidator.validate

class TestSpec extends FunSpec with Matchers {
  case class Person(firstName: String, lastName: String, age: Int = 18)
  case class Post(title: String, author: Person)

  case class Account(currency: String, amount: Double)

  class RegularClass(val bar: String)

  /*
  TODO
  regular expressions
  object members
  extensibility
   */

  describe("null checks") {
    import talos.DefaultConstraints._

    it("should not allow nulls for validated fields") {
      implicit val c = constraint[Person](p => p.lastName.isRequired)

      validate(Person("John", null)) shouldEqual Failure
    }
  }

  describe("range checks") {
    import DefaultConstraints._

    it("should be in range (with step)") {
      implicit val c = constraint[Person](p => p.age.isInRange(1, 10, 2))

      validate(Person("John", "Doe", 1)) shouldEqual Success
      validate(Person("John", "Doe", 5)) shouldEqual Success
      validate(Person("John", "Doe", 10)) shouldEqual Failure
      validate(Person("John", "Doe", 11)) shouldEqual Failure
      validate(Person("John", "Doe", 2)) shouldEqual Failure
      validate(Person("John", "Doe", 30)) shouldEqual Failure
    }

    it("should be in range (no step) - integer values") {
      implicit val c = constraint[Person](p => p.age.isInRange(20, 50))

      validate(Person("John", "Doe", 20)) shouldEqual Success
      validate(Person("John", "Doe", 50)) shouldEqual Success
      validate(Person("John", "Doe", 33)) shouldEqual Success

      validate(Person("John", "Doe", 11)) shouldEqual Failure
      validate(Person("John", "Doe", 51)) shouldEqual Failure
    }

    it("should be in range (no step) - floating point values") {
      implicit val c = constraint[Account](a => a.amount.isInRange(1.5, 450.4))

      validate(Account("EUR", 1.5)) shouldEqual Success
      validate(Account("USD", 450.4)) shouldEqual Success
      validate(Account("USD", 420.67)) shouldEqual Success

      validate(Account("EUR", 1.26)) shouldEqual Failure
      validate(Account("USD", 499.7)) shouldEqual Failure
    }

    it("should check min values - integer") {
      implicit val c = constraint[Person](p => p.age.minValue(18))

      validate(Person("John", "Doe", 18)) shouldEqual Success
      validate(Person("John", "Doe", 21)) shouldEqual Success
      validate(Person("John", "Doe", 11)) shouldEqual Failure
    }

    it("should check min values - floating point"){
      implicit val c = constraint[Account](a => a.amount.minValue(2000))

      validate(Account("USD", 2000)) shouldEqual Success
      validate(Account("USD", 2000.001)) shouldEqual Success
      validate(Account("USD", 1999.999)) shouldEqual Failure
    }

    it("should check max values - integer"){
      implicit val c = constraint[Person](p => p.age.maxValue(21))

      validate(Person("John", "Doe", 18)) shouldEqual Success
      validate(Person("John", "Doe", 21)) shouldEqual Success
      validate(Person("John", "Doe", 35)) shouldEqual Failure
    }

    it("should check max values - floating point"){
      implicit val c = constraint[Account](a => a.amount.maxValue(2000))

      validate(Account("USD", 2000)) shouldEqual Success
      validate(Account("USD", 2000.001)) shouldEqual Failure
      validate(Account("USD", 1999.999)) shouldEqual Success
    }
  }

  describe("String validations") {
    import DefaultConstraints._

    it("should check for empty strings") {
      implicit val c = constraint[Person](p => p.firstName.isRequired)

      validate(Person("John", "Doe")) shouldEqual Success
      validate(Person("John", "")) shouldEqual Success
      validate(Person("", "Doe")) shouldEqual Failure
    }
  }

  describe("Composite validations") {

    object MyConstraints extends DefaultConstraints {
      implicit val personConstraint = constraint[Person] { p =>
        p.firstName.isRequired && p.lastName.isRequired && p.age.minValue(21)
      }

      implicit val postConstraint = constraint[Post] { p =>
        p.title.isRequired && p.author.isValid
      }
    }

    import MyConstraints._

    it("should validate object members") {
      validate(Post("test", Person("John", "Doe", 23))) shouldEqual Success
      validate(Post("", Person("John", "Doe", 23))) shouldEqual Failure
      validate(Post("test", Person("", "Doe", 23))) shouldEqual Failure
      validate(Post("test", Person("John", "Doe"))) shouldEqual Failure
    }
  }

  describe("regular classes") {
    import talos.DefaultConstraints._

    it("should work") {
      implicit val c = constraint[RegularClass](obj => obj.bar.isRequired)

      validate(new RegularClass("foo")) shouldEqual Success
      validate(new RegularClass(null)) shouldEqual Failure
      validate(new RegularClass("")) shouldEqual Failure
    }
  }

  describe("Simple Validations") {

    object MyConstraints extends DefaultConstraints {
      implicit val personConstraint = constraint[Person] { p =>
        p.firstName.isRequired && p.lastName.isRequired && p.age.isInRange(18, 50)
      }

      implicit val postConstraint = constraint[Post] { p =>
        p.title.isRequired
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
      validate(Person("John", "Doe", 16)) shouldEqual Failure
    }
  }
}
