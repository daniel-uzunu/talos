import scala.reflect.ClassTag
import scala.reflect.macros.blackbox
import scala.language.experimental.macros
import scala.reflect.runtime.{universe => ru}

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

  object Reflection {
    def getValue[T: ru.TypeTag : ClassTag, S](obj: T, memberName: String): S = {
      val objType = ru.typeTag[T].tpe
      val termSymbol = objType.member(ru.TermName(memberName)).asMethod

      val m = ru.runtimeMirror(obj.getClass.getClassLoader)
      val im = m.reflect(obj)

      im.reflectMethod(termSymbol).apply().asInstanceOf[S]
    }
  }

  object Macro {
    def getValue[T, S](obj: T, memberName: String): S = macro impl[T, S]

    def impl[T: c.WeakTypeTag, S: c.WeakTypeTag](c: blackbox.Context)(obj: c.Tree, memberName: c.Tree): c.Tree = {
      import c.universe._

      val objType = weakTypeOf[T]
      val memberType = weakTypeOf[S]

      memberName match {
        case Literal(Constant(constantMemberName: String)) =>
          q"$obj.${TermName(constantMemberName)}"
        case _ =>
          q"""
            Map[String, () => Any](
              "test" -> $obj.test _,
              "amount" -> $obj.amount _
            )($memberName).apply().asInstanceOf[$memberType]
          """
      }
    }
  }
}
