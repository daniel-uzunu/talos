package object talos {
  def validate[T](obj: T)(implicit c: Constraint[T], mappable: Mappable[T]): Result = {
    val map = mappable.toMap(obj)

    (!mappable.isCaseClass || !map.exists { case (_ , fn) => fn() == null }) && c.verify(map)
  }
}
