package talos

import org.scalatest.{Matchers, FunSpec}

class TestSpec extends FunSpec with Matchers {
  case class Person(firstName: String, lastName: String)

  describe("Simple Validations") {

    implicit object MyConstraints extends Constraints {
      val personConstraint = constraint[Person] {
        p => p.firstName != ""
      }

      override val constraints = List(personConstraint)
    }

    it("should generate a constraint") {
      MyConstraints.personConstraint shouldEqual NotEmptyConstraint[Person]("firstName")
    }

    it("should fail") {
      MyConstraints.personConstraint.tag should not equal NotEmptyConstraint[String]("firstName").tag
    }

    it("should check for empty strings") {
      validate(Person("John", "Doe")) shouldEqual Success
      //validate(Person("", "Doe")) shouldEqual Failure
    }
  }
}
