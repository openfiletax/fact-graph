package gov.irs.factgraph.compnodes

import gov.irs.factgraph.Expression
import gov.irs.factgraph.Path

final class RootNode extends CompNode:
  type Value = Nothing
  override def ValueClass = throw new UnsupportedOperationException(
    "RootNode has no value class",
  )

  val expr: Expression[Nothing] = Expression.Constant(None)

  override private[compnodes] def switch(
      cases: List[(BooleanNode, CompNode)],
  ): CompNode = this

  override private[compnodes] def dependency(path: Path): CompNode = this

  override private[factgraph] def fromExpression(
      expr: Expression[Nothing],
  ): CompNode =
    throw new UnsupportedOperationException(
      "cannot create a root node from an expression",
    )
