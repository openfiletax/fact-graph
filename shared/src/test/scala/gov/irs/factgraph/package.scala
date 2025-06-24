package gov.irs

import gov.irs.factgraph.monads.*
import gov.irs.factgraph.operators.*
import gov.irs.factgraph.compnodes.CompNode

package object factgraph:
  given FactDictionary = FactDictionary()

  val completeExpr: Expression.Constant[Boolean] =
    Expression.Constant(Some(true))

  val incompleteExpr: Expression.Constant[Boolean] =
    Expression.Constant[Boolean](None)

  val placeholderExpr: Expression.Unary[Boolean, Boolean] = Expression.Unary(
    completeExpr,
    new UnaryOperator[Boolean, Boolean]:
      override def apply(x: Result[Boolean]): Result[Boolean] =
        Result.Placeholder(x.value.get)
      protected def operation(x: Boolean): Boolean = ???
  )

  val placeholderFalseExpr: Expression.Unary[Boolean, Boolean] =
    Expression.Unary(
      Expression.Constant(Some(false)),
      new UnaryOperator[Boolean, Boolean]:
        override def apply(x: Result[Boolean]): Result[Boolean] =
          Result.Placeholder(x.value.get)
        protected def operation(x: Boolean): Boolean = ???
    )

  def canaryExpr(cb: => Unit): Expression.Unary[Boolean, Boolean] =
    Expression.Unary(
      completeExpr,
      new UnaryOperator[Boolean, Boolean]:
        override def apply(x: Result[Boolean]): Result[Boolean] =
          cb
          x
        protected def operation(x: Boolean): Boolean = ???
    )
