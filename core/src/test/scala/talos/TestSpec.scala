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

    var d = "sss"

    def test: String = {
      println("aaa")
      name
    }

    def `a-b`: String = "a"
  }

  describe("get value reflection") {
    val account = new BankAccount("main", 1000)

    import JavaReflection.getValue

    it("should return the value for case class field") {
      getValue(account, "name") shouldEqual "main"
      getValue(account, "amount") shouldEqual 1000
      getValue(account, "toString") should not equal "BankAccount(main,1001)"
      getValue(account, "d") shouldEqual "sss"

      val s = "test"
      getValue(account, s) shouldEqual "main"
      getValue(account, s) shouldEqual "main"
    }

    it("should fail") {
      // TODO: throw better exception
      //an [IndexOutOfBoundsException] should be thrownBy getValue(account, "convert")
    }
  }
}
