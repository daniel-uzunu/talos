package talos

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.runtime.universe._

sealed trait Result

case object Success extends Result
case object Failure extends Result

case class Member[A <: AnyRef, B](name: String, declaringClass: Class[A], memberType: Class[B])

sealed trait Constraint[T] {}

case class NotEmptyConstraint[T <:  AnyRef](member: Member[T, String]) extends Constraint[T]

trait DefaultConstraints {
  def constraint[T <: AnyRef](fn: T => Constraint[T]): Constraint[T] = fn(null.asInstanceOf[T])

  implicit def toConstraint[T <: AnyRef : TypeTag](expr: => Boolean): Constraint[T] =
    NotEmptyConstraint[T](Member("firstName", typeOf[T].getClass.asInstanceOf[Class[T]], classOf[String]))
}

object DefaultConstraints extends DefaultConstraints
