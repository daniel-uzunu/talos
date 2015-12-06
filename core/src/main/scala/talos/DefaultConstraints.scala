package talos

import scala.language.implicitConversions
import scala.reflect.macros.blackbox
import scala.language.experimental.macros

trait DefaultConstraints {
  def constraint[T >: Null](fn: T => Constraint[T]): Constraint[T] = fn(null)

  implicit class ConstraintOperators[T](self: Constraint[T]) {
    def &&(other: Constraint[T]): Constraint[T] = AndConstraint(self, other)
    def ||(other: Constraint[T]): Constraint[T] = OrConstraint(self, other)
  }

  implicit def toStringWrapper[T](value: String): StringWrapper[T] = macro DefaultConstraintsImpl.toStringWrapper[T]

  implicit def toNumericWrapper[T, N](value: N)(implicit ev: Numeric[N]): NumericWrapper[T, N] =
    macro DefaultConstraintsImpl.toNumericWrapper[T, N]
}

object DefaultConstraints extends DefaultConstraints

case class StringWrapper[T](memberName: String) {
  def isRequired: Constraint[T] = NotEmptyConstraint(memberName)
}

case class NumericWrapper[T, N: Numeric](memberName:  String) {
  def isInRange(min: N, max: N, step: N): Constraint[T] = RangeConstraint(memberName, Some(min), Some(max), Some(step))
  def isInRange(min: N, max: N): Constraint[T] = RangeConstraint(memberName, Some(min), Some(max), None)
  def minValue(min: N): Constraint[T] = RangeConstraint(memberName, Some(min), None, None)
  def maxValue(max: N): Constraint[T] = RangeConstraint(memberName, None, Some(max), None)
}

object DefaultConstraintsImpl {
  def toStringWrapper[T: c.WeakTypeTag](c: blackbox.Context)(value: c.Tree): c.Tree = {
    import c.universe._

    val q"$obj.$memberName" = value
    q"StringWrapper(${memberName.decodedName.toString})"
  }

  def toNumericWrapper[T: c.WeakTypeTag, N: c.WeakTypeTag](c: blackbox.Context)(value: c.Tree)(ev: c.Tree): c.Tree = {
    import c.universe._

    val q"$obj.$memberName" = value
    q"NumericWrapper(${memberName.decodedName.toString})"

  }
}
