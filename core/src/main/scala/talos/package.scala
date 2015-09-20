
package object talos {

  def validate[A <: AnyRef](obj: A)(implicit constraint: Constraint[A]): Result = {

    // TODO: I should use macros instead of reflection
    def getValue[T](obj: AnyRef, fieldName: String): T = {
      val field = obj.getClass.getDeclaredField(fieldName)
      field.setAccessible(true)
      field.get(obj).asInstanceOf[T]
    }

    constraint match {
      case NotEmptyConstraint(field) =>
        if (getValue[String](obj, field.name) != "") Success else Failure
    }
  }

  object JavaReflection {
    def getValue[T](obj: T, methodName: String): Any = {
      val method = obj.getClass.getMethod(methodName)
      method.invoke(obj)
    }
  }

  object ScalaReflection {
    import scala.reflect.ClassTag
    import scala.reflect.runtime.universe._

    def getValue[T: TypeTag : ClassTag](obj: T, methodName: String): Any = {
      val symbol = typeOf[T].member(TermName(methodName)).asMethod

      val m = runtimeMirror(obj.getClass.getClassLoader)
      val im = m.reflect(obj)

      im.reflectMethod(symbol).apply()
    }
  }

  object Macro {
    import scala.reflect.macros.blackbox
    import scala.language.experimental.macros

    def getValue[T](obj: T, methodName: String): Any = macro impl[T]

    def impl[T: c.WeakTypeTag](c: blackbox.Context)(obj: c.Tree, methodName: c.Tree): c.Tree = {
      import c.universe._

      val selectedMembers = weakTypeOf[T].members.filter { member =>
        member.isPublic && !member.isConstructor && !member.typeSignature.takesTypeArgs &&
          (member.typeSignature.paramLists.isEmpty || member.typeSignature.paramLists.head.isEmpty)
      }

      val mappings = selectedMembers.map(member => q"${member.name.decodedName.toString} -> $obj.$member _")

      q"Map(..$mappings)($methodName)()"
    }
  }
}
