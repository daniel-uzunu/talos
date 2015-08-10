package talos

import scala.language.experimental.macros
import MacrosImpl._

object Macros {
  def assert(cond: Boolean, msg: Any): Unit = macro assertImpl
}
