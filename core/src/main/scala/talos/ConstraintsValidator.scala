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

  def validate[T](obj: T)(implicit c: Constraint[T], mappable: Mappable[T]): Result = {
    val map = mappable.toMap(obj)

    def checkConstraint(c: Constraint[T]): Result = c match {
      case AndConstraint(c1, c2) => checkConstraint(c1) && checkConstraint(c2)
      case OrConstraint(c1, c2) => checkConstraint(c1) || checkConstraint(c2)
      case mc: MemberConstraint[T, _] =>
        val memberValue = map(mc.memberName)()
        memberValue != null && checkMemberConstraint(mc, memberValue)
    }

    def checkMemberConstraint[U](c: MemberConstraint[T, U], value: U): Result = c match {
      case nec: NotEmptyConstraint[T] => value.asInstanceOf[String] != ""
      case rc: RangeConstraint[T, U] =>
        import Ordering.Implicits._
        import Numeric.Implicits._

        import rc.{min, max, step}

        implicit val n = rc.n

        val verifyMin = min.isEmpty || value >= min.get
        val verifyMax = max.isEmpty || value <= max.get
        val verifyStep = step.isEmpty || (n match {
          case num: Integral[U] => num.mkNumericOps(value - min.get) % step.get == 0
          case num: Fractional[U] => (value - min.get).toDouble() % step.get.toDouble() == 0
        })

        verifyMin && verifyMax && verifyStep
    }

    val hasNulls = mappable.isCaseClass && map.exists { case (_, fn) => fn() == null }
    !hasNulls && checkConstraint(c)
  }
}
