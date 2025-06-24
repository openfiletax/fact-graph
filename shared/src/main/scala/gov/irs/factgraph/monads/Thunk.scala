package gov.irs.factgraph.monads

final case class Thunk[+A](private val f: () => A):
  lazy val get: A = f()

  def map[B](f1: A => B): Thunk[B] =
    Thunk(() => f1(f()))

  def flatMap[B](f1: (=> A) => Thunk[B]): Thunk[B] =
    f1({ f() })
