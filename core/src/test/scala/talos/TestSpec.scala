package talos

import org.scalatest.{Matchers, FunSpec}

class TestSpec extends FunSpec with Matchers {
  case class Person(firstName: String, lastName: String)
  case class Post(title: String, author: Person)

  describe("Simple Validations") {

    object MyConstraints extends DefaultConstraints {
      implicit val personConstraint = constraint[Person] { p =>
        p.firstName != ""
      }

      implicit val postConstraint = constraint[Post] { p =>
        p.title != ""
      }
    }

    import MyConstraints._


    ignore("should generate a constraint") {
      personConstraint shouldEqual NotEmptyConstraint[Person](Member("firstName", classOf[Person], classOf[String]))
      //postConstraint shouldEqual NotEmptyConstraint[Post]("title")
    }

    it("should check for empty strings") {
      validate(Person("John", "Doe")) shouldEqual Success
      validate(Person("", "Doe")) shouldEqual Failure
    }
  }

  case class BankAccount(name: String, amount: Int) {
    def convert(rate: Double): Double = amount * rate

    def test: String = {
      println("aaa")
      name
    }
  }

  describe("get value reflection") {
    val account = BankAccount("main", 1000)

    import Macro.getValue

    it("should return the value for case class field") {
      getValue[BankAccount, String](account, "name") shouldEqual "main"
      getValue[BankAccount, Int](account, "amount") shouldEqual 1000
      getValue[BankAccount, String](account, "toString") shouldEqual "BankAccount(main,1000)"

      val s = "test"
      getValue[BankAccount, String](account, s) shouldEqual "main"
      getValue[BankAccount, String](account, s) shouldEqual "main"
    }

    it("should fail") {
      // TODO: throw better exception
      //an [IndexOutOfBoundsException] should be thrownBy getValue(account, "convert")
    }
  }
}
