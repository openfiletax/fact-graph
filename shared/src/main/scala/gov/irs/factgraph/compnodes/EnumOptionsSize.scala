package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.monads.{MaybeVector, Result, Thunk}
import gov.irs.factgraph.operators.{AggregateOperator, BinaryOperator, UnaryOperator}

object EnumOptionsSize extends CompNodeFactory:
  override val Key: String = "EnumOptionsSize"
  private val operator = EnumOptionsSizeOperator()
  def apply(node: EnumOptionsNode): IntNode =
    IntNode(
      Expression.Unary(
        node.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: EnumOptionsNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

private final class EnumOptionsSizeOperator() extends UnaryOperator[Int, List[String]]:
  override protected def operation(options: List[String]): Int =
    options.length
