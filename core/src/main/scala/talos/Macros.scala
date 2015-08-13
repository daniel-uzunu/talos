package talos

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Macros {
  def constraintImpl[T: c.WeakTypeTag](c: blackbox.Context)(fn: c.Expr[T => Boolean]): c.Expr[Constraint[T]] = {
    import c.universe._

    c.Expr[Constraint[T]]{
      q"""
        talos.NotEmptyConstraint[Person]("firstName")
      """
    }
  }
}
