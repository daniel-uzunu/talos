package object talos {
  def validate[T](obj: T)(implicit c: Constraint[T], mappable: Mappable[T]): Result = c.verify(mappable.toMap(obj))
}
