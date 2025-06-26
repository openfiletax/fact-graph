package gov.irs.factgraph.operators

import gov.irs.factgraph.monads.*
import gov.irs.factgraph.{Explanation, Expression, Factual}

trait Arity4Operator[+A, -W, -X, -Y, -Z] extends Operator:
  protected def operation(arg1: W, arg2: X, arg3: Y, arg4: Z): A

  def apply(
      arg1: Result[W],
      arg2: Result[X],
      arg3: Result[Y],
      arg4: Result[Z],
  ): Result[A] =
    if (
      arg1 == Result.Incomplete || arg2.get == Result.Incomplete || arg3.get == Result.Incomplete || arg4.get == Result.Incomplete
    ) Result.Incomplete
    else
      val value = operation(arg1.get, arg2.get, arg3.get, arg4.get)
      val complete =
        arg1.complete && arg2.complete && arg3.complete && arg4.complete

      Result(value, complete)

  final def apply(
      arg1: MaybeVector[Result[W]],
      arg2: MaybeVector[Result[X]],
      arg3: MaybeVector[Result[Y]],
      arg4: MaybeVector[Result[Z]],
  ): MaybeVector[Result[A]] =
    MaybeVector.vectorize4(apply, arg1, arg2, arg3, arg4)

  final def thunk(
      arg1: Thunk[Result[W]],
      arg2: Thunk[Result[X]],
      arg3: Thunk[Result[Y]],
      arg4: Thunk[Result[Z]],
  ): Thunk[Result[A]] =
    Thunk(() => this(arg1.get, arg2.get, arg3.get, arg4.get))

  final def thunk(
      arg1: MaybeVector[Thunk[Result[W]]],
      arg2: MaybeVector[Thunk[Result[X]]],
      arg3: MaybeVector[Thunk[Result[Y]]],
      arg4: MaybeVector[Thunk[Result[Z]]],
  ): MaybeVector[Thunk[Result[A]]] =
    MaybeVector.vectorize4(thunk, arg1, arg2, arg3, arg4)

  def explain(
      arg1: Expression[_],
      arg2: Expression[_],
      arg3: Expression[_],
      arg4: Expression[_],
  )(using Factual): MaybeVector[Explanation] =
    MaybeVector.vectorize4(
      (
          arg1: Explanation,
          arg2: Explanation,
          arg3: Explanation,
          arg4: Explanation,
      ) => Explanation.opWithInclusiveChildren(arg1, arg2, arg3, arg4),
      arg1.explain,
      arg2.explain,
      arg3.explain,
      arg4.explain,
    )
