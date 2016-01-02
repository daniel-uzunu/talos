package talos

import scala.language.implicitConversions

sealed trait Result {
  def &&(other: => Result): Result
  def ||(other: => Result): Result
}

object Result {
  implicit def fromBool(b: Boolean): Result = if (b) Success else Failure
}

case object Success extends Result {
  def &&(other: => Result) = other
  def ||(other: => Result) = Success
}
case object Failure extends Result {
  def &&(other: => Result) = Failure
  def ||(other: => Result) = other
}

object ConstraintsValidator {
  /**
    * Verifies if an object meets the constraint.
    *
    * @param obj the object to be validate
    * @param c the constraint against which to verify the object
    * @return the result of the validation
    */
  def validate[T](obj: T)(implicit c: Constraint[T]): Result = c match {
    case AndConstraint(c1, c2) => validate(obj)(c1) && validate(obj)(c2)
    case OrConstraint(c1, c2) => validate(obj)(c1) || validate(obj)(c2)
    case mc: MemberConstraint[T, _] =>
      val method = obj.getClass.getMethod(mc.memberName)
      val memberValue = method.invoke(obj)
      memberValue != null && checkMemberConstraint(mc, memberValue)
  }

  private def checkMemberConstraint[T, U](c: MemberConstraint[T, U], value: U): Result = c match {
    case notEqualConstraint: NotEmptyConstraint[T] => value.asInstanceOf[String] != ""
    case refConstraint: RefConstraint[T, U] => validate(value)(refConstraint.constraint)
    case rangeConstraint: RangeConstraint[T, U] =>
      import Ordering.Implicits._
      import Numeric.Implicits._

      import rangeConstraint.{min, max, step}
      implicit val n = rangeConstraint.n

      val verifyMin = min.isEmpty || value >= min.get
      val verifyMax = max.isEmpty || value <= max.get
      val verifyStep = step.isEmpty || (n match {
        case num: Integral[U] => num.mkNumericOps(value - min.get) % step.get == 0
        case num: Fractional[U] => (value - min.get).toDouble() % step.get.toDouble() == 0
      })

      verifyMin && verifyMax && verifyStep
  }
}
