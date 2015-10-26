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

sealed trait Constraint[T] {
  def verify(map: Map[String, () => Any]): Result

  def &&(other: Constraint[T]): Constraint[T] = AndConstraint(this, other)
  def ||(other: Constraint[T]): Constraint[T] = OrConstraint(this, other)
}

sealed trait MemberConstraint[T, U] extends Constraint[T] {
  def memberName: String
  override def verify(map: Map[String, () => Any]) = verify(map(memberName)().asInstanceOf[U])
  def verify(value: U): Result
}

case class NotEmptyConstraint[T](memberName: String) extends MemberConstraint[T, String] {
  override def verify(value: String) = value != ""
}

case class RangeConstraint[T, N: Numeric](memberName: String, min: Option[N], max: Option[N], step: Option[N])
  extends MemberConstraint[T, N] {

  import Ordering.Implicits._
  import Numeric.Implicits._

  override def verify(value: N) = verifyMin(value) && verifyMax(value) && verifyStep(value)

  private def verifyMin(value: N) = min.isEmpty || value >= min.get

  private def verifyMax(value: N) = max.isEmpty || value <= max.get

  private def verifyStep(value: N) = {
    step.isEmpty || (implicitly[Numeric[N]] match {
      case num: Integral[N] => num.mkNumericOps(value - min.get) % step.get == 0
      case num: Fractional[N] => (value - min.get).toDouble() % step.get.toDouble() == 0
    })
  }
}

case class AndConstraint[T](c1: Constraint[T], c2: Constraint[T]) extends Constraint[T] {
  override def verify(map: Map[String, () => Any]) = c1.verify(map) && c2.verify(map)
}
case class OrConstraint[T](c1: Constraint[T], c2: Constraint[T]) extends Constraint[T] {
  override def verify(map: Map[String, () => Any]) = c1.verify(map) || c2.verify(map)
}

case class StringWrapper[T](memberName: String) {
  def isRequired: Constraint[T] = NotEmptyConstraint(memberName)
}

case class NumericWrapper[T, N: Numeric](memberName:  String) {
  def isInRange(min: N, max: N, step: N): Constraint[T] = RangeConstraint(memberName, Some(min), Some(max), Some(step))
  def isInRange(min: N, max: N): Constraint[T] = RangeConstraint(memberName, Some(min), Some(max), None)
  def minValue(min: N): Constraint[T] = RangeConstraint(memberName, Some(min), None, None)
  def maxValue(max: N): Constraint[T] = RangeConstraint(memberName, None, Some(max), None)
}

trait DefaultConstraints {
  import scala.reflect.macros.blackbox
  import scala.language.experimental.macros

  def constraint[T >: Null](fn: T => Constraint[T]): Constraint[T] = fn(null)

  implicit def toStringWrapper[T](value: String): StringWrapper[T] = macro DefaultConstraints.toStringWrapperImpl[T]

  implicit def toNumericWrapper[T, N](value: N)(implicit ev: Numeric[N]): NumericWrapper[T, N] =
    macro DefaultConstraints.toNumericWrapperImpl[T, N]
}

object DefaultConstraints extends DefaultConstraints {
  import scala.reflect.macros.blackbox
  import scala.language.experimental.macros

  def toStringWrapperImpl[T: c.WeakTypeTag](c: blackbox.Context)(value: c.Tree): c.Tree = {
    import c.universe._

    val q"$obj.$memberName" = value
    q"StringWrapper(${memberName.decodedName.toString})"
  }

  def toNumericWrapperImpl[T: c.WeakTypeTag, N: c.WeakTypeTag](c: blackbox.Context)(value: c.Tree)(ev: c.Tree): c.Tree = {
    import c.universe._

    val q"$obj.$memberName" = value
    q"NumericWrapper(${memberName.decodedName.toString})"

  }
}

trait Mappable[T] {
  def toMap(obj: T): Map[String, () => Any]
}

object Mappable {
  import scala.reflect.macros.blackbox
  import scala.language.experimental.macros

  implicit def materialize[T]: Mappable[T] = macro materializeImpl[T]

  // TODO: differentiate between case classes and regular classes
  def materializeImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val tpe = weakTypeOf[T]

    val selectedMembers = tpe.members.filter { member =>
      member.isPublic && member.isMethod &&
        !member.isConstructor && !member.typeSignature.takesTypeArgs &&
        (member.typeSignature.paramLists.isEmpty || member.typeSignature.paramLists.head.isEmpty)
    }

    val mappings = selectedMembers.map { member =>
      q"${member.name.decodedName.toString} -> obj.$member _"
    }

    q"""
      new Mappable[$tpe] {
        def toMap(obj: $tpe) = Map(..$mappings)
      }
    """
  }
}
