package object talos {
  def validate[A](obj: A)(implicit constraints: Constraints): Result = Success
}
