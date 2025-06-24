package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.monads.{MaybeVector, Result, Thunk}
import gov.irs.factgraph.operators.{AggregateOperator, BinaryOperator, UnaryOperator}

import scala.util.matching.Regex

object Regex extends CompNodeFactory:
  override val Key: String = "Regex"
  private val operator = RegexOperator()
  def apply(node: StringNode, pattern: StringNode): BooleanNode =
    BooleanNode(
      Expression.Binary(
        node.expr,
        pattern.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val lhs = CompNode.getConfigChildNode(e, "Input")
    val rhs = CompNode.getConfigChildNode(e, "Pattern")
    if (!lhs.isInstanceOf[StringNode] || !rhs.isInstanceOf[StringNode])
      throw new IllegalArgumentException(
        "Input and Pattern should be string nodes",
      )
    this(lhs.asInstanceOf[StringNode], rhs.asInstanceOf[StringNode])

private final class RegexOperator() extends BinaryOperator[Boolean, String, String]:
  override protected def operation(x: String, pattern: String): Boolean =
    pattern.r.matches(x);
