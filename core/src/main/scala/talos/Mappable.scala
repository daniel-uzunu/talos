package talos

import scala.reflect.macros.blackbox
import scala.language.experimental.macros

trait Mappable[T] {
  def toMap(obj: T): Map[String, () => Any]
  def isCaseClass: Boolean
}

object Mappable {
  implicit def materialize[T]: Mappable[T] = macro MappableImpl.materialize[T]
}

object MappableImpl {

  def materialize[T: c.WeakTypeTag](c: blackbox.Context): c.Tree = {
    import c.universe._

    val tpe = weakTypeOf[T]

    if (!tpe.typeSymbol.isClass) {
      // TODO: throw exception
    }

    val isCaseClass = tpe.typeSymbol.asInstanceOf[ClassSymbol].isCaseClass

    val mappings = tpe.members
      .collect {
        case m: MethodSymbol => m
      }
      .filter {
        if (isCaseClass) m => m.isCaseAccessor
        else m => m.isPublic && m.paramLists.isEmpty && !m.isConstructor && m.typeParams.isEmpty
      }
      .map { member =>
        q"${member.name.decodedName.toString} -> obj.$member _"
      }

    q"""
      new Mappable[$tpe] {
        def toMap(obj: $tpe) = Map(..$mappings)
        def isCaseClass = $isCaseClass
      }
    """
  }
}
