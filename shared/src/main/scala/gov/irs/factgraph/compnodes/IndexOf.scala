package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.{BinaryOperator, CollectOperator}
import gov.irs.factgraph.types.{Collection, CollectionItem}
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}

import java.util.UUID

object IndexOf extends CompNodeFactory:
  override val Key: String = "IndexOf"

  private val operator = IndexOfOperator()
  def apply(collection: CollectionNode, index: IntNode): CollectionItemNode =
    CollectionItemNode(
      Expression.Binary(
        collection.expr,
        index.expr,
        operator,
      ),
      collection.alias,
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val collection = CompNode.getConfigChildNode(e, "Collection")
    val index = CompNode.getConfigChildNode(e, "Index")
    if (!collection.isInstanceOf[CollectionNode] || !index.isInstanceOf[IntNode])
      throw new UnsupportedOperationException(
        s"required to have a collection node and integer index node specified to use indexof",
      )
    this(collection.asInstanceOf[CollectionNode], index.asInstanceOf[IntNode])

private final class IndexOfOperator extends BinaryOperator[CollectionItem, Collection, Int]:
  override def apply(
      lhs: Result[Collection],
      rhs: Thunk[Result[Int]],
  ): Result[CollectionItem] =
    if (lhs == Result.Incomplete || rhs.get == Result.Incomplete) Result.Incomplete
    else
      val index = rhs.get.get
      val collection = lhs.get
      var potentialCollectionItem = collection.items.lift(index)
      potentialCollectionItem match {
        case Some(x) => Result.Complete(CollectionItem(x))
        case None    => Result.Incomplete
      }

  override protected def operation(x: Collection, y: Int): CollectionItem =
    throw new Exception("shouldn't be calling this")
