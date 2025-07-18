package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Explanation, Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.monads.{MaybeVector, Result, Thunk}
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.operators.BinaryOperator

import scala.annotation.unused

object Override extends CompNodeFactory:
  override val Key: String = "Override"

  def apply(
      source: CompNode,
      condition: BooleanNode,
      default: CompNode
  ): CompNode =
    source.fromExpression(
      Expression.Switch(
        List(
          (condition.expr, default.expr.asInstanceOf[Expression[source.Value]]),
          (Expression.Constant(Some(true)), source.expr)
        )
      )
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary
  ): CompNode =
    throw new UnsupportedOperationException(
      "Override should be used directly as a child of a fact"
    )
