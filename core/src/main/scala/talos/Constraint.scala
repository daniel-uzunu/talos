package talos

sealed trait Constraint[T]

case class AndConstraint[T](c1: Constraint[T], c2: Constraint[T]) extends Constraint[T]

case class OrConstraint[T](c1: Constraint[T], c2: Constraint[T]) extends Constraint[T]


sealed trait MemberConstraint[T, +U] extends Constraint[T] {
  def memberName: String
}

case class NotEmptyConstraint[T](memberName: String) extends MemberConstraint[T, String]

case class PatternConstraint[T](memberName: String, pattern: String) extends MemberConstraint[T, String]

case class RangeConstraint[T, N: Numeric](memberName: String, min: Option[N], max: Option[N], step: Option[N])
    extends MemberConstraint[T, N] {

  val n = implicitly[Numeric[N]]
}

case class RefConstraint[T, U](memberName: String, constraint: Constraint[U]) extends MemberConstraint[T, U]
