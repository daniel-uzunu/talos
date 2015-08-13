package talos

import scala.language.experimental.macros
import scala.reflect.ClassTag

sealed trait Result

case object Success extends Result
case object Failure extends Result

trait Constraint[T] {
  def tag: ClassTag[T]
}

case class NotEmptyConstraint[T](fieldName: String)(implicit val tag: ClassTag[T]) extends Constraint[T]

abstract class Constraints {
  def constraints: List[Constraint[_]]

  def constraint[T](fn: T => Boolean): Constraint[T] = macro Macros.constraintImpl[T]
}
