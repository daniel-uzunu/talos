# talos

Talos is a Scala validation library in which the validation rules are stored as data structures. The advantage of this 
approach is that the validation rules can be read and converted to another format. An example, would be the typical 
client-server scenario: the validation rules can be encoded as JSON and sent to the client.

Example:

```scala
case class Person(firstName: String, lastName: String, age: Int)
case class Book(title: String, author: Person, year: Int)
```

```scala
object MyConstraints extends DefaultConstraints {
  implicit val personConstraint = constraint[Person] { p =>
    p.firstName.isRequired && p.lastName.isRequired && p.age.minValue(21)
  }

  implicit val postConstraint = constraint[Book] { b =>
    b.title.isRequired && b.author.isValid b.year.isInRange(1900, 2030)
  }
}
```

```scala
import MyConstraints._

validate(Person("John", "Doe", 18)) //Failure
validate(Person("John", "Doe", 21)) //Success

validate(Book("Some Title", Person("John", "Doe", 30), 2001)) //Success
```
