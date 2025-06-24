package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.BinaryOperator
import gov.irs.factgraph.types.{Collection, CollectionItem}
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}

object FirstNCollectionItems extends CompNodeFactory:
  override val Key: String = "FirstNCollectionItems"

  private val operator = FirstNCollectionItemsOperator()

  def apply(collection: CollectionNode, count: IntNode): CollectionNode =
    CollectionNode(
      Expression.Binary(
        collection.expr,
        count.expr,
        operator,
      ),
      collection.alias,
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val collection = CompNode.getConfigChildNode(e, "Collection")
    val count = CompNode.getConfigChildNode(e, "Count")
    if (!collection.isInstanceOf[CollectionNode] || !count.isInstanceOf[IntNode])
      throw new UnsupportedOperationException(
        s"required to have a collection node and integer count node specified to use FirstNCollectionItems",
      )
    this(collection.asInstanceOf[CollectionNode], count.asInstanceOf[IntNode])

private final class FirstNCollectionItemsOperator extends BinaryOperator[Collection, Collection, Int]:
  override def apply(
      lhs: Result[Collection],
      rhs: Thunk[Result[Int]],
  ): Result[Collection] =
    if (lhs == Result.Incomplete || rhs.get == Result.Incomplete)
      Result.Incomplete
    else
      (lhs, rhs.get) match
        case (Result(collection, collComplete), Result(count, countComplete)) =>
          Result(
            Collection(collection.items.slice(0, count)),
            collComplete && countComplete,
          )
        case _ => Result.Incomplete

  override protected def operation(x: Collection, y: Int): Collection =
    throw new Exception("shouldn't be calling this")
