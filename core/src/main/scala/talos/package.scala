package object talos {
  def validate[T](obj: T)(implicit constraint: Constraint[T], validatable: Validatable[T]): Result = {
    constraint match {
      case c: MemberConstraint[T] => memberConstraint(validatable.getValue(obj, c.memberName), c)

      case AndConstraint(c1, c2) => validate(obj)(c1, validatable) && validate(obj)(c2, validatable)
      case OrConstraint(c1, c2) => validate(obj)(c1, validatable) || validate(obj)(c2, validatable)
    }
  }

  private def memberConstraint[T](value: Any, constraint: MemberConstraint[T]): Result = {
    constraint match {
      case NotEmptyConstraint(_) => value != ""
      case RelationalConstraint(_, num, op) => relationalConstraint(value.asInstanceOf[AnyVal], num, op)
    }
  }

  private def relationalConstraint(value: AnyVal, num: AnyVal, op: RelationalOp): Result = {
    import NumberOrdering._

    op match {
      case LessThan           => value < num
      case LessThanOrEqual    => value <= num
      case GreaterThan        => value > num
      case GreaterThanOrEqual => value >= num
      case Equal              => value == num
      case NotEqual           => value != num
    }
  }

  private object NumberOrdering extends Ordering[AnyVal] {
    override def compare(x: AnyVal, y: AnyVal) = (x, y) match {
      case (x: Short, y: Short)   => Ordering.Short.compare(x, y)
      case (x: Short, y: Int)     => Ordering.Int.compare(x, y)
      case (x: Short, y: Long)    => Ordering.Long.compare(x, y)
      case (x: Short, y: Float)   => Ordering.Float.compare(x, y)
      case (x: Short, y: Double)  => Ordering.Double.compare(x, y)

      case (x: Int, y: Short)     => Ordering.Int.compare(x, y)
      case (x: Int, y: Int)       => Ordering.Int.compare(x, y)
      case (x: Int, y: Long)      => Ordering.Long.compare(x, y)
      case (x: Int, y: Float)     => Ordering.Float.compare(x, y)
      case (x: Int, y: Double)    => Ordering.Double.compare(x, y)

      case (x: Long, y: Short)    => Ordering.Long.compare(x, y)
      case (x: Long, y: Int)      => Ordering.Long.compare(x, y)
      case (x: Long, y: Long)     => Ordering.Long.compare(x, y)
      case (x: Long, y: Float)    => Ordering.Float.compare(x, y)
      case (x: Long, y: Double)   => Ordering.Double.compare(x, y)

      case (x: Float, y: Short)   => Ordering.Float.compare(x, y)
      case (x: Float, y: Int)     => Ordering.Float.compare(x, y)
      case (x: Float, y: Long)    => Ordering.Float.compare(x, y)
      case (x: Float, y: Float)   => Ordering.Float.compare(x, y)
      case (x: Float, y: Double)  => Ordering.Double.compare(x, y)

      case (x: Double, y: Short)  => Ordering.Double.compare(x, y)
      case (x: Double, y: Int)    => Ordering.Double.compare(x, y)
      case (x: Double, y: Long)   => Ordering.Double.compare(x, y)
      case (x: Double, y: Float)  => Ordering.Double.compare(x, y)
      case (x: Double, y: Double) => Ordering.Double.compare(x, y)

      case (_, _)                 => throw new RuntimeException("Not numeric")
    }
  }
}
