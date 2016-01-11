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

  implicit def toAnyRefWrapper[T, U](value: U): AnyRefWrapper[T, U] = macro DefaultConstraintsImpl.toAnyRefWrapper[T, U]
}

object DefaultConstraints extends DefaultConstraints

case class StringWrapper[T](memberName: String) {
  def isRequired: Constraint[T] = NotEmptyConstraint(memberName)
  def pattern(regex: String): Constraint[T] = PatternConstraint(memberName, regex)
}

case class NumericWrapper[T, N: Numeric](memberName:  String) {
  def isInRange(min: N, max: N, step: N): Constraint[T] = RangeConstraint(memberName, Some(min), Some(max), Some(step))
  def isInRange(min: N, max: N): Constraint[T] = RangeConstraint(memberName, Some(min), Some(max), None)
  def minValue(min: N): Constraint[T] = RangeConstraint(memberName, Some(min), None, None)
  def maxValue(max: N): Constraint[T] = RangeConstraint(memberName, None, Some(max), None)
}

case class AnyRefWrapper[T, U](memberName: String) {
  def isValid(implicit c: Constraint[U]): Constraint[T] = RefConstraint(memberName, c)
}

class DefaultConstraintsImpl(val c: blackbox.Context) {
  import c.universe._

  def toStringWrapper[T: c.WeakTypeTag](value: c.Tree): c.Tree = {
    val q"$obj.$memberName" = value
    q"StringWrapper(${memberName.decodedName.toString})"
  }

  def toNumericWrapper[T: c.WeakTypeTag, N: c.WeakTypeTag](value: c.Tree)(ev: c.Tree): c.Tree = {
    val q"$obj.$memberName" = value
    q"NumericWrapper(${memberName.decodedName.toString})"
  }

  def toAnyRefWrapper[T: c.WeakTypeTag, U: c.WeakTypeTag](value: c.Tree): c.Tree = {
    val q"$obj.$memberName" = value
    q"AnyRefWrapper(${memberName.decodedName.toString})"
  }
}
