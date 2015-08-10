package talos

import scala.reflect.macros.blackbox

object MacrosImpl {
  def assertImpl(c: blackbox.Context) (cond: c.Expr[Boolean], msg: c.Expr[Any]) : c.Expr[Unit] = {
    import c.universe._

    println(cond.toString())

    c.Expr[Unit](Literal(Constant(())))
  }
}
