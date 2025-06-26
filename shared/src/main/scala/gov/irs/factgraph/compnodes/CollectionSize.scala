package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.{ReduceOperator, UnaryOperator}
import gov.irs.factgraph.types.Collection

object CollectionSize extends CompNodeFactory:
  override val Key: String = "CollectionSize"
  private val operator = CollectionSizeOperator()
  def apply(node: CollectionNode): IntNode =
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
      case x: CollectionNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

private final class CollectionSizeOperator extends UnaryOperator[Int, Collection]:
  override protected def operation(x: Collection): Int =
    x.items.length
