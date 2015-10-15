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

sealed trait RelationalOp

case object LessThan extends RelationalOp
case object LessThanOrEqual extends RelationalOp
case object GreaterThan extends RelationalOp
case object GreaterThanOrEqual extends RelationalOp
case object Equal extends RelationalOp
case object NotEqual extends RelationalOp

sealed trait Constraint[T]

sealed trait MemberConstraint[T] extends Constraint[T] {
  def memberName: String
}

case class NotEmptyConstraint[T](memberName: String) extends MemberConstraint[T]

case class RelationalConstraint[T](memberName: String, num: AnyVal, op: RelationalOp) extends MemberConstraint[T]

case class AndConstraint[T](c1: Constraint[T], c2: Constraint[T]) extends Constraint[T]
case class OrConstraint[T](c1: Constraint[T], c2: Constraint[T]) extends Constraint[T]

object Constraint {
  import scala.language.experimental.macros
  import scala.reflect.macros.blackbox

  implicit def toConstraint[T](expr: Boolean): Constraint[T] = macro toConstraintImpl[T]

  def toConstraintImpl[T: c.WeakTypeTag](c: blackbox.Context)(expr: c.Tree): c.Tree = {
    import c.universe._

    val tpe = weakTypeOf[T]

    expr match {
      case q""" $obj.$memberName != "" """ =>
        q"NotEmptyConstraint[$tpe](${memberName.decodedName.toString})"

      case q"$obj.$memberName > $value" =>
        q"RelationalConstraint[$tpe](${memberName.decodedName.toString}, $value, GreaterThan)"
      case q"$obj.$memberName < $value" =>
        q"RelationalConstraint[$tpe](${memberName.decodedName.toString}, $value, LessThan)"

      case q"$expr1 && $expr2" =>
        q"AndConstraint(${toConstraintImpl(c)(expr1)}, ${toConstraintImpl(c)(expr2)})"
      case q"$expr1 || $expr2" =>
        q"OrConstraint(${toConstraintImpl(c)(expr1)}, ${toConstraintImpl(c)(expr2)})"
    }
  }
}

trait DefaultConstraints {
  def constraint[T >: Null](fn: T => Constraint[T]): Constraint[T] = fn(null)
}

object DefaultConstraints extends DefaultConstraints

trait Validatable[T] {
  def getValue(obj: T, memberName: String): Any
}

object Validatable {
  import scala.reflect.macros.blackbox
  import scala.language.experimental.macros

  implicit def materialize[T]: Validatable[T] = macro materializeImpl[T]

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
        new Validatable[$tpe] {
          def getValue(obj: $tpe, memberName: String): Any = {
            val membersMap = Map(..$mappings)
            val fn = membersMap(memberName)
            fn()
          }
        }
      """
  }
}
