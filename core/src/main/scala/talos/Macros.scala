package talos

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object Macros {
  def constraintImpl[T: c.WeakTypeTag](c: Context)(constraint: c.Tree): c.Expr[Constraint[T]] = {
    import c.universe._

    c.Expr[Constraint[T]]{
      q"""
        val t: T = null
        import t._
        talos.NotEmptyConstraint[Person]("firstName")
      """
    }
  }
}
